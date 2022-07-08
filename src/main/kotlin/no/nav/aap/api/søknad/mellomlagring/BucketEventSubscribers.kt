package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import com.google.cloud.storage.NotificationInfo.EventType.valueOf
import com.google.cloud.storage.Storage
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.brukernotifikasjoner.JPADittNavBeskjedRepository
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager.Companion.FNR
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.transaction.annotation.Transactional
import java.util.*

@ConditionalOnGCP
class VedleggEventSubscriber(mapper: ObjectMapper, client: DittNavClient,
                             private val storage: Storage,
                             private val cfgs: BucketsConfig) :
    AbstractEventSubscriber(mapper, client, storage, cfgs.vedlegg, cfgs.id) {

    override fun receiver() =
        MessageReceiver { message, consumer ->
            log.info("Id: ${message.messageId}")
            log.info("Data: ${message.attributesMap}")
            val resource = mapper.readValue(message.data.toStringUtf8(), Map::class.java)
            log.info("Resource representation: $resource")
            consumer.ack()
        }
}

@Suppress("BlockingMethodInNonBlockingContext")
@ConditionalOnGCP
class MellomlagringEventSubscriber(mapper: ObjectMapper, client: DittNavClient,
                                   private val storage: Storage,
                                   private val repo: JPADittNavBeskjedRepository,
                                   private val cfgs: BucketsConfig) :
    AbstractEventSubscriber(mapper, client, storage, cfgs.mellom, cfgs.id) {

    @Transactional
    override fun receiver() =
        MessageReceiver { message, consumer ->
            with(message) {
                log.info("Data: $attributesMap")
                val resource = mapper.readValue(data.toStringUtf8(), Map::class.java).also {
                    log.info("Resource representation: $it")
                }
                when (typeFra(attributesMap[EVENT_TYPE])) {
                    OBJECT_FINALIZE -> {
                        if (containsAttributes(OVERWROTEGENERATION)) {
                            log.trace("Oppdatert mellomlagring")
                        }
                        else {
                            log.trace("Førstegangs mellomlagring")
                            metadataFra(resource)?.let {
                                with(it) {
                                    log.info("Oppretter beskjed med UUID $uuid")
                                    dittNav.opprettBeskjed(type, uuid, fnr, "Du har en påbegynt søknad om AAP")
                                }
                            } ?: log.warn("Fant ikke forventet metadata i $resource")
                        }
                    }
                    OBJECT_DELETE -> {
                        if (containsAttributes(OVERWRITTEBBYGENERATION)) {
                            log.trace("Delete pga opppdatert mellomlagring")
                        }
                        else {
                            log.trace("Delete pga avslutt eller timeout")
                            metadataFra(resource)?.let { metadata ->
                                with(metadata) {
                                    val l = repo.getEventidByFnrAndDoneFalseOOrderByCreated(fnr.fnr)?.firstOrNull()
                                    log.info("Fikk uuis $l")
                                    l?.let {
                                        val eventId = UUID.fromString(it)
                                        log.info("Avslutter beskjed med UUID $eventId")
                                        dittNav.avsluttBeskjed(type, fnr, eventId)
                                    } ?: log.warn("Fant ikke uuid for melding")
                                }
                            } ?: log.warn("Fant ikke forventet metadata i $resource")
                        }
                    }
                    else -> log.trace("Event type ${attributesMap[EVENT_TYPE]} ikke håndtert")
                }
            }
            consumer.ack()
        }

    private fun typeFra(type: String?) = type?.let { valueOf(it) }

    @Suppress("UNCHECKED_CAST")
    private fun metadataFra(resource: Map<*, *>) =
        resource[METADATA]?.let { meta ->
            meta as Map<String, String>
        }?.let { meta ->
            val uuid = meta[UUID_]?.let { UUID.fromString(it) }
            val fnr = meta[FNR]?.let { Fødselsnummer(it) }
            val type = meta[SKJEMATYPE]?.let { SkjemaType.valueOf(it) }
            if (uuid != null && fnr != null && type != null) {
                Metadata(type, fnr, uuid)
            }
            else null
        }

    private data class Metadata(val type: SkjemaType, val fnr: Fødselsnummer, val uuid: UUID)
}