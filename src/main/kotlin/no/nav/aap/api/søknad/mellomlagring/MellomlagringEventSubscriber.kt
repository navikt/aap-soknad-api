package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import org.springframework.boot.CommandLineRunner
import no.nav.aap.api.config.Metrikker
import no.nav.aap.api.config.Metrikker.Companion.MELLOMLAGRING_EXPIRED
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.endeligSlettet
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.eventType
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.førstegangsOpprettelse
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.metadata
import no.nav.aap.util.LoggerUtil.getLogger

//@ConditionalOnGCP
class MellomlagringEventSubscriber(private val minside: MinSideClient,
                                   private val cfg: BucketConfig,
                                   private val mapper: ObjectMapper,
                                   private val metrikker: Metrikker,
                                   private val subscriber: PubSubSubscriberTemplate) : CommandLineRunner {

    private val log = getLogger(javaClass)

    override fun run(vararg args: String?) {
        with(cfg.mellom) {
            log.info("IAC Abonnererer på hendelser i $subscription")
            subscriber.subscribe(subscription.navn) { event ->
                event.ack()
                with(event.pubsubMessage) {
                    val eventType = eventType()
                    metadata(mapper)?.let { md ->
                        log.trace("Event type $eventType med metadata $md")
                        with(md) {
                            when (eventType) {
                                OBJECT_FINALIZE -> if (førstegangsOpprettelse()) {
                                    minside.opprettUtkast(fnr, "Du har en påbegynt $tittel",type, eventId).also {
                                    }
                                }
                                else {
                                    minside.oppdaterUtkast(fnr, "Du har en påbegynt $tittel", type)
                                }
                                OBJECT_DELETE -> if (endeligSlettet()) {
                                    md.varighet()?.let {
                                        log.info("Endelig slettet etter ${it.toSeconds()}s")
                                        if (it > cfg.mellom.varighet) {
                                            metrikker.inc(MELLOMLAGRING_EXPIRED)
                                            log.info("Slettet endelig mellomlagring etter ${cfg.mellom.varighet.toDays()} dager for $md")
                                        }
                                    }
                                    minside.avsluttUtkast(fnr, type)
                                }
                                else {
                                    Unit.also {
                                        log.trace("Slettet grunnet ny versjon, ingen oppdatering av utkast for $fnr")
                                    }
                                }
                                else -> log.warn("Event $eventType ikke håndtert (dette skal aldri skje)")
                            }
                        }
                    } ?: log.warn("Fant ikke forventede metadata i event ${event.pubsubMessage} $attributesMap")
                }
            }
        }
    }
}