package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.stereotype.Component
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.Companion.STORAGE_CHANNEL
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.GCPEventType.FØRSTEGANGS
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.GCPEventType.OPPDATERING
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.GCPEventType.SLETTET
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.MellomlagringsHendelse
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.util.LoggerUtil


@Component
class MellomlagringEventSubscriber(private val minside: MinSideClient, private val cfg: BucketConfig, private val mapper: ObjectMapper) //: MessageHandler
{

    private val log = LoggerUtil.getLogger(javaClass)


  /*   override fun handleMessage(m : Message<out Any>) {
        m.headers.get(ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage::class.java)?.let {
            handle(it)
        }
    }*/


    @ServiceActivator(inputChannel = STORAGE_CHANNEL)
    fun handle(h: MellomlagringsHendelse) {
        h.metadata?.let {
            log.trace("Event type {} med metadata {}", h.type,it)
            when(h.type) {
                FØRSTEGANGS ->  minside.opprettUtkast(it.fnr, "Du har en påbegynt $it.", it.type, it.eventId)
                OPPDATERING -> minside.oppdaterUtkast(it.fnr, "Du har en påbegynt ${it.tittel}", it.type)
                SLETTET -> minside.avsluttUtkast(it.fnr, it.type)
                else -> log.warn("Event ${h.type} ikke håndtert (dette skal aldri skje)")
            }
        }    ?: log.warn("Fant ikke forventede metadata i event}")
    }
    /*
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


    private fun slettet(msg : PubsubMessage,  md : Metadata) =
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

     */
}