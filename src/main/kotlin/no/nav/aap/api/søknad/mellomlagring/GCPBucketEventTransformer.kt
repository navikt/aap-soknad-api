package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders.*
import com.google.cloud.storage.NotificationInfo.EventType
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import org.springframework.integration.annotation.Transformer
import org.springframework.messaging.handler.annotation.Header
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.ENDELIG_SLETTET
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.IGNORERT
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.OPPDATERT
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.OPPRETTET
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.Metadata
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.endeligSlettet
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.førstegangsOpprettelse
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.metadata
import no.nav.aap.util.LoggerUtil

class GCPBucketEventTransformer(private val mapper: ObjectMapper) {

    private val log = LoggerUtil.getLogger(javaClass)

    @Transformer
    fun payload(@Header(ORIGINAL_MESSAGE) msg : BasicAcknowledgeablePubsubMessage,@Header("eventType") et: EventType)  =
        try {
            msg.pubsubMessage.let {
                val md = it.metadata(mapper)
                log.trace("Metadata er {}, et er $et", md)
                when (et) {
                    OBJECT_FINALIZE -> if (it.førstegangsOpprettelse()) MellomlagringsHendelse(OPPRETTET,md) else MellomlagringsHendelse(OPPDATERT,md)
                    OBJECT_DELETE -> if (it.endeligSlettet()) MellomlagringsHendelse(ENDELIG_SLETTET,md) else MellomlagringsHendelse(IGNORERT,md)
                    else -> MellomlagringsHendelse(IGNORERT)
                }
            }
        } catch (e: Exception) {
            log.warn("OOPS",e)
            MellomlagringsHendelse(IGNORERT)
        }

    enum class GCPEventType {
        OPPRETTET, OPPDATERT,ENDELIG_SLETTET, IGNORERT
    }
    data class MellomlagringsHendelse(val type : GCPEventType, val metadata : Metadata? = null)
}