package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders.ORIGINAL_MESSAGE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import com.google.pubsub.v1.PubsubMessage
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.stereotype.Component
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.GCPEventType.ENDELIG_SLETTING
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.GCPEventType.OPPDATERING
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.GCPEventType.OPPRETTET
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.MellomlagringsHendelse
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.Metadata
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.endeligSlettet
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.eventType
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.førstegangsOpprettelse
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.metadata
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.util.LoggerUtil


@Component
class MellomlagringEventSubscriber(private val minside: MinSideClient, private val cfg: BucketConfig, private val mapper: ObjectMapper) : MessageHandler
{

    private val log = LoggerUtil.getLogger(javaClass)


    override fun handleMessage(m : Message<out Any>) {
        m.headers.get(ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage::class.java)?.let {
            handle(it)
        }
    }

     private fun handle(msg : BasicAcknowledgeablePubsubMessage) =
        msg.pubsubMessage.metadata(mapper)?.let {md ->
            val eventType = msg.pubsubMessage.eventType().also {
                log.trace("Event type {} med metadata {}", it, md)
            }
            when (eventType) {
                OBJECT_FINALIZE -> oppdatert(msg.pubsubMessage, md)
                OBJECT_DELETE -> slettet(msg.pubsubMessage, md)
                else -> log.warn("Event $eventType ikke håndtert (dette skal aldri skje)")
            }
        } ?: log.warn("Fant ikke forventede metadata i event $this ${msg.pubsubMessage.attributesMap}")


    private fun slettet(msg : PubsubMessage, md : Metadata) =
        if (msg.endeligSlettet()) {
            md.varighet()?.let {
                log.info("Endelig slettet etter ${it.toSeconds()}s")
                if (it > cfg.mellom.varighet) {
                    log.info("Slettet endelig mellomlagring etter ${cfg.mellom.varighet.toDays()} dager for $md")
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

@Component
class NyMellomllagringEventSubscriber(private val minside: MinSideClient) {
    private val log = LoggerUtil.getLogger(javaClass)

    @ServiceActivator(inputChannel = MellomlagringBeanConfig.STORAGE_CHANNEL)
    fun handle(h: MellomlagringsHendelse) =
        try {
            h.metadata?.let { md ->
                log.trace("Event type {} med metadata {}", h.type,md)
                when(h.type) {
                    OPPRETTET ->  minside.opprettUtkast(md.fnr, "Du har en påbegynt $md.", md.type, md.eventId).also {
                        log.trace("Opprettet {}", it)
                    }
                    OPPDATERING -> minside.oppdaterUtkast(md.fnr, "Du har en påbegynt ${md.tittel}", md.type).also {
                        log.trace("Oppdatert {}", it)
                    }
                    ENDELIG_SLETTING -> minside.avsluttUtkast(md.fnr, md.type).also {
                        log.trace("Endelig slettet {}", it)
                    }
                    else -> log.trace("Event {} ignorert", h.type)
                }
            }    ?: log.warn("Fant ikke forventede metadata i event}")
        } catch (e: Exception) {
            log.warn("OOPS",e)
        }
}