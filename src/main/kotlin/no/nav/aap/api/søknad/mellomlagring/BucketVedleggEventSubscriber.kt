package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.VedleggBucketCfg
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class BucketVedleggEventSubscriber(private val cfgs: BucketsConfig) {
    private val log = LoggerUtil.getLogger(javaClass)

    init {
        log.info("Abonnerer på events for vedlegg")
        subscribe(cfgs.vedlegg, cfgs.team)
        log.info("Abonnerert på events for vedlegg OK $cfgs")
    }

    private fun subscribe(cfg: VedleggBucketCfg, id: String) {
        val subscriptionName = ProjectSubscriptionName.of(id, cfg.subscription)
        // Instantiate an asynchronous message receiver.
        val receiver = MessageReceiver { message: PubsubMessage, consumer: AckReplyConsumer ->
            // Handle incoming message, then ack the received message.
            log.info("Id: ${message.messageId}")
            log.info("Data: ${message.attributesMap}")
            consumer.ack()
        }
        var subscriber: Subscriber? = null
        try {
            subscriber = Subscriber.newBuilder(subscriptionName, receiver).build()
            // Start the subscriber.
            subscriber.startAsync().awaitRunning()
            log.info("Listening for messages on $subscriptionName")
            // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
            subscriber.awaitRunning()

            //subscriber.awaitTerminated(300, TimeUnit.SECONDS)
        }
        catch (e: Exception) {
            // Shut down the subscriber after 30s. Stop receiving messages.
            subscriber!!.stopAsync()
        }
    }
}