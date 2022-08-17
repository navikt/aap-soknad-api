package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import com.google.cloud.storage.NotificationInfo.EventType.valueOf
import com.google.pubsub.v1.PubsubMessage
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavNotifikasjonType.Companion.SØKNADSTD
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.SKJEMATYPE
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.UUID_
import no.nav.aap.api.søknad.mellomlagring.MellomlagringEventSubscriber.Metadata.Companion.getInstance
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.toMDC
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
@ConditionalOnGCP
class MellomlagringEventSubscriber(private val dittNav: DittNavClient,
                                   private val cfg: BucketConfig,
                                   private val pubSub: PubSubSubscriberTemplate) {

    private val log = getLogger(javaClass)

    init {
        subscribe()
    }

    private fun subscribe() =
        with(cfg.mellom) {
            log.trace("Abonnererer på hendelser i $subscription")
            pubSub.subscribe(subscription.navn) { msg ->
                msg.ack()
                with(msg.pubsubMessage) {
                    val type = eventType()
                    log.trace(CONFIDENTIAL, "Data i $type event er ${data.toStringUtf8()}")
                    log.trace(CONFIDENTIAL, "attributter i $type event er $attributesMap")
                    when (type) {
                        OBJECT_FINALIZE -> opprettet(metadata())
                        OBJECT_DELETE -> slettet(metadata())
                        else -> log.warn("Event type $type ikke håndtert (dette skal aldri skje)")
                    }
                }
            }
        }

    private fun opprettet(msg: Metadata?) =
        msg?.let {
            with(it) {
                log.trace(CONFIDENTIAL, "Oppretter beskjed fra metadata $it")
                dittNav.opprettBeskjed(SØKNADSTD, uuid, fnr, "Du har en påbegynt ${type.tittel}", true)
            }
        } ?: log.warn("Fant ikke forventede metadata")

    private fun slettet(msg: Metadata?) =
        msg?.let {
            with(it) {
                log.trace(CONFIDENTIAL, "Sletter beskjed fra metadata $it")
                dittNav.avsluttBeskjed(type, fnr, uuid)
            }
        } ?: log.warn("Fant ikke forventede metadata")

    private fun PubsubMessage.data() = MAPPER.readValue<Map<String, Any>>(data.toStringUtf8())
    private fun PubsubMessage.objektNavn() = attributesMap[OBJECTID]?.split("/")
    private fun PubsubMessage.eventType() =
        attributesMap[EVENT_TYPE]?.let { valueOf(it) }.also { t ->
            log.trace("Event type er $t")
        }

    private fun PubsubMessage.metadata(): Metadata? =
        with(objektNavn()) {
            if (this?.size == 2) {
                data()[METADATA]?.let {
                    it as Map<String, String>
                    getInstance(it[SKJEMATYPE], this[0], it[UUID_])
                }
            }
            else null
        }

    private data class Metadata private constructor(val type: SkjemaType, val fnr: Fødselsnummer, val uuid: UUID) {
        companion object {
            fun getInstance(type: String?, fnr: String?, uuid: String?): Metadata? {
                return if (uuid != null && fnr != null && type != null) {
                    toMDC(NAV_CALL_ID, uuid)
                    Metadata(SkjemaType.valueOf(type), Fødselsnummer(fnr), UUID.fromString(uuid))
                }
                else {
                    null
                }
            }
        }
    }

    companion object {
        private val MAPPER = ObjectMapper().registerModule(KotlinModule.Builder().build())
        private const val EVENT_TYPE = "eventType"
        private const val METADATA = "metadata"
        private const val OBJECTID = "objectId"
    }
}