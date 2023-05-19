package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders.ORIGINAL_MESSAGE
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import no.nav.aap.api.config.Metrikker
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.handle
import no.nav.aap.util.LoggerUtil

class StoragePubMessageHandler(private val minside: MinSideClient, private val cfg: BucketConfig, private val mapper: ObjectMapper, private val  metrikker: Metrikker) :
    MessageHandler {

    private val log = LoggerUtil.getLogger(javaClass)

    override fun handleMessage(m : Message<*>) {
        log.trace("PubSub handling")
        m.headers.get(ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage::class.java)?.let {
            it.apply {
                log.trace("PubSub acking ${it.pubsubMessage.messageId}")
                ack()
                pubsubMessage.handle(minside,cfg,mapper,metrikker)
            }
        }
    }
}