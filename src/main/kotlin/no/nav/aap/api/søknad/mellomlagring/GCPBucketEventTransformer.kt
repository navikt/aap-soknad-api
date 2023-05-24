package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import org.springframework.integration.annotation.Transformer
import org.springframework.messaging.handler.annotation.Header
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.ENDELIG_SLETTING
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.IGNORER
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.OPPDATERING
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.OPPRETTET
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.Metadata
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.endeligSlettet
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.eventType
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.førstegangsOpprettelse
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.metadata
import no.nav.aap.util.LoggerUtil

class GCPBucketEventTransformer {

    private val log = LoggerUtil.getLogger(javaClass)

    @Transformer
    fun payload(@Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) msg : BasicAcknowledgeablePubsubMessage?)  =
        try {
            msg?.pubsubMessage?.let {
                val md = it.metadata(jacksonObjectMapper())
                log.trace("Metadata er {}", md)
                when ( it.eventType()) {
                    OBJECT_FINALIZE -> if (it.førstegangsOpprettelse()) MellomlagringsHendelse(OPPRETTET,md) else MellomlagringsHendelse(OPPDATERING,md)
                    OBJECT_DELETE -> if (it.endeligSlettet()) MellomlagringsHendelse(ENDELIG_SLETTING,md) else MellomlagringsHendelse(IGNORER,md)
                    else -> MellomlagringsHendelse(IGNORER,md)
                }
            } ?: MellomlagringsHendelse(IGNORER)
        } catch (e: Exception) {
            MellomlagringsHendelse(IGNORER)
        }

    enum class GCPEventType {
        OPPRETTET, OPPDATERING,ENDELIG_SLETTING, IGNORER
    }
    data class MellomlagringsHendelse(val type : GCPEventType, val metadata : Metadata? = null)
}