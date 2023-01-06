package no.nav.aap.api.søknad.minside

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.cloud.storage.NotificationInfo.EventType
import com.google.pubsub.v1.PubsubMessage
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.CREATED
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.SKJEMATYPE
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.UUID_
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.toMDC
import no.nav.aap.util.StringExtensions.decap

object PubSubMessageExtensions {

    private const val OVERWRITTEN = "overwrittenByGeneration"
    private const val OVERWROTE ="overwroteGeneration"
    private const val EVENT_TYPE = "eventType"
    private const val METADATA = "metadata"
    private const val OBJECTID = "objectId"

    private val log = LoggerUtil.getLogger(javaClass)

    fun PubsubMessage.metadata(mapper: ObjectMapper) =
        with(objektNavn()) {
            if (this?.size == 2) {
                data(mapper)[METADATA]?.let {
                    val map = it as Map<String, String>
                    Metadata.getInstance(map[SKJEMATYPE], this[0], map[UUID_], map[CREATED])
                }
            }
            else {
                null
            }
        }
    fun PubsubMessage.data(mapper: ObjectMapper) = mapper.readValue<Map<String, Any>>(data.toStringUtf8())
     private fun PubsubMessage.objektNavn() = attributesMap[OBJECTID]?.split("/")
     fun PubsubMessage.endeligSlettet() = attributesMap[OVERWRITTEN] == null
    fun PubsubMessage.førstegangsOpprettelse() = attributesMap[OVERWROTE] == null

    fun PubsubMessage.eventType() = attributesMap[EVENT_TYPE]?.let { EventType.valueOf(it) }


    data class Metadata private constructor(val type: SkjemaType, val fnr: Fødselsnummer, val eventId: UUID, val created: LocalDateTime? = null) {
        val tittel = type.tittel.decap()

        fun varighet() = created?.let {  Duration.between(it, LocalDateTime.now())}

        companion object {
            fun getInstance(type: String?, fnr: String?, eventId: String?, created: String?) =
                if (eventId != null && fnr != null && type != null) {
                    toMDC(NAV_CALL_ID, eventId)
                    try {
                        val c = created?.let {  LocalDateTime.parse(it) }
                        Metadata(SkjemaType.valueOf(type), Fødselsnummer(fnr), UUID.fromString(eventId),c)
                    }
                    catch (e: Exception)  {
                        log.info("OOPS",e)
                        Metadata(SkjemaType.valueOf(type), Fødselsnummer(fnr), UUID.fromString(eventId))
                    }
                }
                else {
                    null
                }
        }
    }
}