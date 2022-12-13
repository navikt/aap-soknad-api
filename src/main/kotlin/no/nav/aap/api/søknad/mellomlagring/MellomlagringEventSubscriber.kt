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
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.varighet
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.decap
import no.nav.boot.conditionals.ConditionalOnDev
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.boot.CommandLineRunner

@ConditionalOnDev
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
                        log.trace(CONFIDENTIAL,"Event type $eventType med metadata $md for for ${md.fnr}, attributter er $attributesMap")
                        when (eventType) {
                            OBJECT_FINALIZE -> if (førstegang())  {
                                minside.opprettUtkast(md.fnr, "Du har en påbegynt ${md.type.tittel.decap()}", md.type, md.eventId).also {
                                    log.trace("Opprettet førstegangs utkast for ${md.fnr}")
                                }
                            } else {
                               minside.oppdaterUtkast(md.fnr,"Du har en påbegynt ${md.type.tittel.decap()}",md.type).also {
                                   log.trace("Oppdatert utkast grunnet oppdatering for ${md.fnr}") }
                            }
                            OBJECT_DELETE -> if (endeligSlettet()) {
                                with(md) {
                                    log.info("Slettet utkast endelig hendelse etter ${varighet()}")
                                    minside.avsluttUtkast(fnr, type).also {
                                        log.trace("Endelig slettet utkast for ${md.fnr}")
                                    }
                                }

                            } else {
                                Unit.also {
                                    log.trace("Slettet grunnet ny versjon, ingen oppdatering av utkast for ${md.fnr}")
                                }
                            }
                            else -> log.warn("Event $eventType ikke håndtert (dette skal aldri skje)")
                        }
                    } ?: log.warn("Fant ikke forventede metadata i event ${event.pubsubMessage}")
                }
            }
        }
    }
}