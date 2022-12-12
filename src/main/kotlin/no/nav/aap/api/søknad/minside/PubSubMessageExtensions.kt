package no.nav.aap.api.søknad.minside

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.cloud.storage.NotificationInfo.EventType
import com.google.pubsub.v1.PubsubMessage
import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.SKJEMATYPE
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.UUID_
import no.nav.aap.util.MDCUtil

object PubSubMessageExtensions {

    private const val OVERWRITTEN = "overwrittenByGeneration"
    private const val OVERWROTE ="overwroteGeneration"
    private const val EVENT_TYPE = "eventType"
    private const val METADATA = "metadata"
    private const val OBJECTID = "objectId"
     fun PubsubMessage.metadata(mapper: ObjectMapper) =
        with(objektNavn()) {
            if (this?.size == 2) {
                data(mapper)[METADATA]?.let {
                    it as Map<String, String>
                    Metadata.getInstance(it[SKJEMATYPE], this[0], it[UUID_])
                }
            }
            else { null}
        }
     private fun PubsubMessage.data(mapper: ObjectMapper) = mapper.readValue<Map<String, Any>>(data.toStringUtf8())
     private fun PubsubMessage.objektNavn() = attributesMap[OBJECTID]?.split("/")
     fun PubsubMessage.endeligSlettet() = attributesMap[OVERWRITTEN] == null
    fun PubsubMessage.førstegang() = attributesMap[OVERWROTE] == null

    fun PubsubMessage.eventType() = attributesMap[EVENT_TYPE]?.let { EventType.valueOf(it) }


    data class Metadata private constructor(val type: SkjemaType, val fnr: Fødselsnummer, val eventId: UUID) {
        companion object {
            fun getInstance(type: String?, fnr: String?, eventId: String?) =
                if (!(eventId == null || fnr == null || type == null)) {
                    MDCUtil.toMDC(MDCUtil.NAV_CALL_ID, eventId)
                    Metadata(SkjemaType.valueOf(type), Fødselsnummer(fnr), UUID.fromString(eventId))
                }
                else {
                    null
                }
        }
    }
}