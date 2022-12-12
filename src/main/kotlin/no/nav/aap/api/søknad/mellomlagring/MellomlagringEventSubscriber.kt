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
import no.nav.boot.conditionals.ConditionalOnDev
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.boot.CommandLineRunner

@ConditionalOnDev
class MellomlagringEventSubscriber(private val dittNav: MinSideClient,
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
                    val type = eventType()
                    log.trace(CONFIDENTIAL, "Data i $type er ${data.toStringUtf8()}, attributter er $attributesMap")
                    metadata(mapper)?.let {
                        log.info("PubSub event $type med metadata $it")
                        when (type) {
                            OBJECT_FINALIZE -> if (førstegang())  {
                                dittNav.opprettUtkast(it.fnr, "Du har en påbegynt ${it.type.tittel.decap()}", it.type, it.eventId).also {
                                    log.trace("Opprettet førstegangs utkast")
                                }
                            } else {
                                log.trace("Oppdatering av mellomlagring NOOP")
                               // dittNav.oppdaterUtkast(it.fnr,"Du har en påbegynt ${it.type.tittel.decap()}",it.type).also {
                               //     log.trace("Oppdatert utkast grunnet oppdatering") }
                            }
                            OBJECT_DELETE -> if (endeligSlettet()) {
                                dittNav.avsluttUtkast(it.fnr, it.type).also {
                                    log.trace("Endelig slettet utkast")
                                }
                            } else {
                                Unit.also {
                                    log.trace("Slettet grunnet ny versjon, ingen oppdatering av utkast")
                                }
                            }
                            else -> log.warn("Event $type ikke håndtert (dette skal aldri skje)")
                        }
                    } ?: log.warn("Fant ikke forventede metadata i event ${event.pubsubMessage}")
                }
            }
        }
    }
}