package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders.ORIGINAL_MESSAGE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import com.google.pubsub.v1.PubsubMessage
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.MellomlagringBucketConfig
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.Metadata
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.endeligSlettet
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.eventType
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.førstegangsOpprettelse
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.metadata
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.util.LoggerUtil

class MellomlagringEventSubscriber(private val minside: MinSideClient, private val cfg: MellomlagringBucketConfig, private val mapper: ObjectMapper) :
    MessageHandler {

    private val log = LoggerUtil.getLogger(javaClass)

    override fun handleMessage(m : Message<out Any>) {
        m.headers.get(ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage::class.java)?.let {
            handle(it.pubsubMessage)
        }
    }
    private fun handle(msg : PubsubMessage) =
        msg.metadata(mapper)?.let {
            log.trace("Event type {} med metadata {}", msg.eventType(), it)
            when (msg.eventType()) {
                OBJECT_FINALIZE -> oppdatert(msg, it)
                OBJECT_DELETE -> slettet(msg, it)
                else -> log.warn("Event ${msg.eventType()} ikke håndtert (dette skal aldri skje)")
            }
        } ?: log.warn("Fant ikke forventede metadata i event $this ${msg.attributesMap}")


    private fun slettet(msg : PubsubMessage,  md : Metadata) =
        if (msg.endeligSlettet()) {
            md.varighet()?.let {
                log.info("Endelig slettet etter ${it.toSeconds()}s")
                if (it > cfg.varighet) {
                    log.info("Slettet endelig mellomlagring etter ${cfg.varighet.toDays()} dager for $md")
                }
            }
            minside.avsluttUtkast(md.fnr, md.type)
        }
        else {
            Unit.also {
                log.trace("Slettet grunnet ny versjon, ingen oppdatering av utkast for {}", md.fnr)
            }
        }

    private fun oppdatert(msg : PubsubMessage, md : Metadata)  =
        with(md) {
            if (msg.førstegangsOpprettelse()) {
                minside.opprettUtkast(fnr, "Du har en påbegynt $tittel", type, eventId)
            }
            else {
                minside.oppdaterUtkast(fnr, "Du har en påbegynt $tittel", type)
            }
        }
}