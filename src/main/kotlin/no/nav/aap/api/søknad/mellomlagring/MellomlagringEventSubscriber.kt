package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.endeligSlettet
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.eventType
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.førstegang
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.metadata
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.decap
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.boot.CommandLineRunner

@ConditionalOnGCP
class MellomlagringEventSubscriber(private val minside: MinSideClient,
                                   private val cfg: BucketConfig,
                                   private val mapper: ObjectMapper,
                                   private val subscriber: PubSubSubscriberTemplate) : CommandLineRunner {

    private val log = getLogger(javaClass)

    override fun run(vararg args: String?) {
        with(cfg.mellom) {
            log.trace("IAC Abonnererer på hendelser i $subscription")
            subscriber.subscribe(subscription.navn) { event ->
                event.ack()
                with(event.pubsubMessage) {
                    val eventType = eventType()
                    metadata(mapper)?.let { md ->
                        log.trace("Event type $eventType med metadata $md and map $attributesMap")
                        when (eventType) {
                            OBJECT_FINALIZE -> if (førstegang()) {
                                minside.opprettUtkast(md.fnr,
                                        "Du har en påbegynt ${md.type.tittel.decap()}",
                                        md.type,
                                        md.eventId).also {
                                    log.trace("Opprettet muligens førstegangs utkast for ${md.fnr}")
                                }
                            }
                            else {
                                minside.oppdaterUtkast(md.fnr, "Du har en påbegynt ${md.type.tittel.decap()}", md.type)
                                    .also {
                                        log.trace("Oppdaterte muligens utkast grunnet oppdatering for ${md.fnr}")
                                    }
                            }

                            OBJECT_DELETE -> if (endeligSlettet()) {
                                with(md) {
                                    log.info("Slettet muligens utkast endelig hendelse for $md")
                                    minside.avsluttUtkast(fnr, type).also {
                                        log.info("Endelig muligens slettet utkast for ${md.fnr}")
                                    }
                                }
                            }
                            else {
                                Unit.also {
                                    log.trace("Slettet grunnet ny versjon, ingen oppdatering av utkast for ${md.fnr}")
                                }
                            }

                            else -> log.warn("Event $eventType ikke håndtert (dette skal aldri skje)")
                        }
                    } ?: log.warn("Fant ikke forventede metadata i event ${event.pubsubMessage} $attributesMap")
                }
            }
        }
    }
}