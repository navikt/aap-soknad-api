package no.nav.aap.api.søknad.minside

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.cloud.storage.NotificationInfo.EventType
import com.google.pubsub.v1.PubsubMessage
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.toKotlinDuration
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.SKJEMATYPE
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.UUID_
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.toMDC
import no.nav.aap.util.StringExtensions.partialMask

object PubSubMessageExtensions {

    private const val OVERWRITTEN = "overwrittenByGeneration"
    private const val OVERWROTE ="overwroteGeneration"
    private const val EVENT_TYPE = "eventType"
    private const val METADATA = "metadata"
    private const val OBJECTID = "objectId"
    private const val TIMECREATED = "timeCreated"

    private val log = LoggerUtil.getLogger(javaClass)

    fun PubsubMessage.metadata(mapper: ObjectMapper) =
        with(objektNavn()) {
            if (this?.size == 2) {
                data(mapper)[METADATA]?.let {
                    val map = it as Map<String, String>
                    val md = Metadata.getInstance(map[SKJEMATYPE], this[0], map[UUID_])
                    md
                }
            }
            else {
                null
            }
        }
     private fun PubsubMessage.data(mapper: ObjectMapper) = mapper.readValue<Map<String, Any>>(data.toStringUtf8())
     private fun PubsubMessage.objektNavn() = attributesMap[OBJECTID]?.split("/")
     fun PubsubMessage.endeligSlettet() = attributesMap[OVERWRITTEN] == null
    fun PubsubMessage.varighet() = attributesMap[TIMECREATED]?.let {  Duration.between(ZonedDateTime.parse(it).toLocalDateTime(), LocalDateTime.now()).toKotlinDuration()}
    fun PubsubMessage.førstegang() = attributesMap[OVERWROTE] == null

    fun PubsubMessage.eventType() = attributesMap[EVENT_TYPE]?.let { EventType.valueOf(it) }


    data class Metadata private constructor(val type: SkjemaType, val fnr: Fødselsnummer, val eventId: UUID) {
        companion object {
            fun getInstance(type: String?, fnr: String?, eventId: String?) =
                if (eventId != null && fnr != null && type != null) {
                    toMDC(NAV_CALL_ID, eventId)
                    Metadata(SkjemaType.valueOf(type), Fødselsnummer(fnr), UUID.fromString(eventId))
                }
                else {
                    null
                }
        }
    }
}