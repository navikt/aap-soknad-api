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
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager.Companion.FNR
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
@ConditionalOnGCP
class MellomlagringEventSubscriber(private val mapper: ObjectMapper,
                                   private val dittNav: DittNavClient,
                                   private val cfg: BucketsConfig) {

    private val log = getLogger(javaClass)

    init {
        abonner()
    }

    private fun abonner() =
        with(cfg) {
            Subscriber.newBuilder(ProjectSubscriptionName.of(project, mellom.subscription.navn),
                    receiver()).build().apply {
                log.trace("Abonnererer på hendelser i ${mellom.subscription}")
                startAsync().awaitRunning()
                awaitRunning() // TODO sjekk dette
            }
        }

    @Transactional
    fun receiver() =
        MessageReceiver { msg, consumer ->
            when (msg.eventType()) {
                OBJECT_FINALIZE -> håndterOpprettet(msg)
                OBJECT_DELETE -> håndterSlettet(msg)
                else -> log.trace("Event type ${msg.eventType()} ikke håndtert")
            }
            consumer.ack()
        }

    private fun håndterOpprettet(msg: PubsubMessage) =
        if (msg.oppdatertMedNyVersjon()) {
            log.trace("Oppdatert mellomlagring med ny versjon")
        }
        else {
            log.trace("Førstegangs mellomlagring")
            håndterFørstegangsMellomlagring(msg)
        }

    private fun håndterSlettet(msg: PubsubMessage) =
        if (msg.slettetGrunnetOppdatering()) {
            log.trace("Sletting pga opppdatert mellomlagring")
        }
        else {
            log.trace("Delete entry pga avslutt eller timeout")
            håndterAvsluttEllerTimeout(msg)
        }

    private fun håndterFørstegangsMellomlagring(msg: PubsubMessage) =
        msg.data.metadata()?.let { md ->
            with(md) {
                log.trace("Oppretter beskjed med UUID $uuid")
                dittNav.opprettBeskjed(type, uuid, fnr, "Du har en påbegynt ${type.tittel}", true)
            }
        } ?: log.warn("Fant ikke forventet metadata")

    private fun håndterAvsluttEllerTimeout(msg: PubsubMessage) =
        msg.data.metadata()?.let { md ->
            with(md) {
                dittNav.eventIdForFnr(fnr)?.let {
                    log.trace("Avslutter beskjed med UUID $it")
                    dittNav.avsluttBeskjed(type, fnr, it)
                } ?: log.warn("Fant ikke UUID for opprinnelig notifikasjon")
            }
        } ?: log.warn("Fant ikke forventet metadata")

    private fun PubsubMessage.eventType() = attributesMap[EVENT_TYPE]?.let { valueOf(it) }
    private fun PubsubMessage.slettetGrunnetOppdatering() = containsAttributes(OVERWRITTEBBYGENERATION)
    private fun PubsubMessage.oppdatertMedNyVersjon() = containsAttributes(OVERWROTEGENERATION)

    @Suppress("UNCHECKED_CAST")
    private fun ByteString.metadata(): Metadata? =
        (mapper.readValue(toStringUtf8(), Map::class.java) as Map<String, Any>)[METADATA]?.let {
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