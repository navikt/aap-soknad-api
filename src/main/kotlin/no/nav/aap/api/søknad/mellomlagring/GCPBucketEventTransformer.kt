package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders.*
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
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.eventType
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.førstegangsOpprettelse
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.metadata
import no.nav.aap.util.LoggerUtil

class GCPBucketEventTransformer(private val mapper: ObjectMapper) {

    private val log = LoggerUtil.getLogger(javaClass)

    @Transformer
    fun payload(@Header(ORIGINAL_MESSAGE) msg : BasicAcknowledgeablePubsubMessage)  =
             msg.pubsubMessage.let { m ->
                 val md = m.metadata(mapper).also {
                     log.trace("Metadata er {}", it)
                 }
                 when (m.eventType()) {
                     OBJECT_FINALIZE -> if (m.førstegangsOpprettelse()) MellomlagringsHendelse(OPPRETTET, md) else MellomlagringsHendelse(OPPDATERT, md)
                     OBJECT_DELETE -> if (m.endeligSlettet()) MellomlagringsHendelse(ENDELIG_SLETTET, md) else MellomlagringsHendelse(IGNORERT, md)
                     else -> MellomlagringsHendelse(IGNORERT)
                 }.also {
                     log.trace("Event oversatt til {}", it)
                 }
             }


    enum class GCPEventType {
        OPPRETTET, OPPDATERT,ENDELIG_SLETTET, IGNORERT
    }
    data class MellomlagringsHendelse(val type : GCPEventType, val metadata : Metadata? = null)
}