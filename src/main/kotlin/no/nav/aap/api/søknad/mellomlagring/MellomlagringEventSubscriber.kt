package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import no.nav.aap.api.config.Metrikker
import no.nav.aap.api.config.Metrikker.Companion.MELLOMLAGRING_EXPIRED
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.data
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.endeligSlettet
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.eventType
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.førstegangsOpprettelse
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.metadata
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.varighet
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.boot.CommandLineRunner

@ConditionalOnGCP
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
                        log.info("Event type $eventType med metadata $md and map $attributesMap")
                        with(md) {
                            when (eventType) {
                                OBJECT_FINALIZE -> if (førstegangsOpprettelse()) {
                                    minside.opprettUtkast(fnr, "Du har en påbegynt $tittel",type, eventId).also {
                                        log.trace("Opprettet muligens førstegangs utkast for $fnr")
                                    }
                                }
                                else {
                                    minside.oppdaterUtkast(fnr, "Du har en påbegynt $tittel", type).also {
                                        log.trace("Oppdaterte muligens utkast grunnet oppdatering for $fnr")
                                    }
                                }
                                OBJECT_DELETE -> if (endeligSlettet()) {
                                    log.info("Endelig slettet ${data(ObjectMapper())}")
                                    varighet()?.let {
                                           log.info("Endelig slettet etter $it")
                                           if (it > cfg.mellom.varighet) {
                                              metrikker.inc(MELLOMLAGRING_EXPIRED)
                                               log.info("Slettet mellomlagring etter ${cfg.mellom.varighet.toDays()} dager for $md")
                                           }
                                       }
                                        log.trace("Slettet muligens utkast endelig hendelse for $md")
                                        minside.avsluttUtkast(fnr, type).also {
                                            log.trace("Endelig muligens slettet utkast for $fnr")
                                    }
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