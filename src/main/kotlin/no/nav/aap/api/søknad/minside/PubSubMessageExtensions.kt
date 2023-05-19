package no.nav.aap.api.søknad.minside

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.cloud.storage.NotificationInfo.EventType
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import com.google.pubsub.v1.PubsubMessage
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import no.nav.aap.api.config.Metrikker
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.mellomlagring.BucketConfig
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.SKJEMATYPE
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.UUID_
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity.Companion.CREATED
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

    fun PubsubMessage.handle(minside: MinSideClient, cfg: BucketConfig, mapper: ObjectMapper, metrikker: Metrikker) =
        with(this) {
            val eventType = eventType()
            lg.trace("PubSub really handling")
            metadata(mapper)?.let { md ->
                log.trace("Event type {} med metadata {}", eventType, md)
                with(md) {
                    when (eventType) {
                        OBJECT_FINALIZE -> if (førstegangsOpprettelse()) {
                            minside.opprettUtkast(fnr, "Du har en påbegynt $tittel", type, eventId).also {
                            }
                        }
                        else {
                            minside.oppdaterUtkast(fnr, "Du har en påbegynt $tittel", type)
                        }

                        OBJECT_DELETE -> if (endeligSlettet()) {
                            md.varighet()?.let {
                                log.info("Endelig slettet etter ${it.toSeconds()}s")
                                if (it > cfg.mellom.varighet) {
                                    metrikker.inc(Metrikker.MELLOMLAGRING_EXPIRED)
                                    log.info("Slettet endelig mellomlagring etter ${cfg.mellom.varighet.toDays()} dager for $md")
                                }
                            }
                            minside.avsluttUtkast(fnr, type)
                        }
                        else {
                            Unit.also {
                                log.trace("Slettet grunnet ny versjon, ingen oppdatering av utkast for {}", fnr)
                            }
                        }

                        else -> log.warn("Event $eventType ikke håndtert (dette skal aldri skje)")
                    }
                }
            } ?: log.warn("Fant ikke forventede metadata i event $this $attributesMap")
        }

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
                        Metadata(SkjemaType.valueOf(type), Fødselsnummer(fnr), UUID.fromString(eventId), created?.let { LocalDateTime.parse(it) })
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