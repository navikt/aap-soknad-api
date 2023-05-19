package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders.ORIGINAL_MESSAGE
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import no.nav.aap.api.config.Metrikker
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.handle

class StoragePubMessageHandler(private val minside: MinSideClient, private val cfg: BucketConfig, private val mapper: ObjectMapper, private val  metrikker: Metrikker) :
    MessageHandler {
    override fun handleMessage(m : Message<*>) {
        m.headers.get(ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage::class.java)?.let {
            it.apply {
                ack()
                pubsubMessage.handle(minside,cfg,mapper,metrikker)
            }
        }
    }
}