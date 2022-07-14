package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import com.google.cloud.storage.NotificationInfo.EventType.valueOf
import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.brukernotifikasjoner.JPADittNavBeskjedRepository
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.MellomlagringBucketConfig
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager.Companion.FNR
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
@ConditionalOnGCP
class MellomlagringEventSubscriber(private val mapper: ObjectMapper,
                                   private val dittNav: DittNavClient,
                                   private val repo: JPADittNavBeskjedRepository,
                                   private val cfgs: BucketsConfig) {

    private val log = getLogger(javaClass)

    init {
        abonner(cfgs.mellom)
    }

    private fun abonner(cfg: MellomlagringBucketConfig) =
        with(ProjectSubscriptionName.of(cfgs.id, cfg.subscription.navn)) {
            log.trace("Abonnererer på events  via subscription $this")
            Subscriber.newBuilder(this, receiver()).build().apply {
                startAsync().awaitRunning()
                awaitRunning() // TODO sjekk dette
            }
        }

    @Transactional
    fun receiver() =
        MessageReceiver { msg, consumer ->
            with(msg) {
                log.trace("Attributer: $attributesMap")
                val metadata = metadataFra(data)
                when (typeFra(attributesMap[EVENT_TYPE])) {
                    OBJECT_FINALIZE -> {
                        håndterOpprettet(metadata, msg)
                    }
                    OBJECT_DELETE -> {
                        håndterSlettet(metadata, msg)
                    }
                    else -> log.trace("Event type ${attributesMap[EVENT_TYPE]} ikke håndtert")
                }
            }
            consumer.ack()
        }

    private fun håndterSlettet(metadata: Metadata?, msg: PubsubMessage) =
        if (msg.slettetGrunnetOppdatering()) {
            log.trace("Sletting pga opppdatert mellomlagring")
        }
        else {
            håndterAvsluttEllerTimeout(metadata)
        }

    private fun håndterAvsluttEllerTimeout(metadata: Metadata?) =
        metadata?.let { md ->
            log.trace("Delete entry pga avslutt eller timeout")
            with(md) {
                repo.getMellomlagretEventIdForFnr(fnr.fnr)?.let { eventId ->
                    UUID.fromString(eventId).also {
                        log.trace("Avslutter beskjed med UUID $it")
                        dittNav.avsluttBeskjed(type, fnr, it)
                    }
                } ?: log.warn("Fant ikke uuid for opprinnelig notifikasjon")
            }
        } ?: log.warn("Fant ikke forventet metadata")

    private fun PubsubMessage.slettetGrunnetOppdatering() = containsAttributes(OVERWRITTEBBYGENERATION)
    private fun PubsubMessage.oppdatertMedNyVersjon() = containsAttributes(OVERWROTEGENERATION)

    private fun håndterOpprettet(metadata: Metadata?, msg: PubsubMessage) =
        if (msg.oppdatertMedNyVersjon()) {
            log.trace("Oppdatert mellomlagring med ny versjon")
        }
        else {
            log.trace("Førstegangs mellomlagring")
            metadata?.let { md ->
                with(md) {
                    log.trace("Oppretter beskjed med UUID $uuid")
                    dittNav.opprettBeskjed(type, uuid, fnr, "Du har en påbegynt ${type.tittel}", true)
                }
            } ?: log.warn("Fant ikke forventet metadata")
        }

    private fun typeFra(type: String?) = type?.let { valueOf(it) }

    @Suppress("UNCHECKED_CAST")
    private fun metadataFra(data: ByteString): Metadata? =
        (mapper.readValue(data.toStringUtf8(), Map::class.java) as Map<String, Any>)[METADATA]?.let {
            it as Map<String, String>
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

    companion object {
        const val EVENT_TYPE = "eventType"
        const val OVERWROTEGENERATION = "overwroteGeneration"
        const val OVERWRITTEBBYGENERATION = "overwrittenByGeneration"
        const val SKJEMATYPE = "skjemaType"
        const val UUID_ = "uuid"
        const val METADATA = "metadata"
    }
}