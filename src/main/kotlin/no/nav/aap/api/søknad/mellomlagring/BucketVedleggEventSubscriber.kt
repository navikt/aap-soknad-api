package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PushConfig.getDefaultInstance
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class BucketVedleggEventSubscriber(private val cfgs: BucketsConfig) {
    private val log = LoggerUtil.getLogger(javaClass)

    init {
        log.info("Abonnerer på events for vedlegg")
        if (!hasTopic()) {
            createTopic()
        }
        else {
            log.info("Topic ${cfgs.vedlegg.topic} finnes allerede for ${cfgs.id}")
        }
        if (!hasSubscription()) {
            createSubscription()
        }
        else {
            log.info("Subscription ${cfgs.vedlegg.subscription} finnes allerede for ${cfgs.id}")
        }
        subscribe().also {
            log.info("Abonnerert på events for vedlegg OK ${cfgs.vedlegg} via subscription ${cfgs.vedlegg.subscription}")
        }
    }

    private fun createTopic() = TopicAdminClient.create().createTopic(TopicName.of(cfgs.id, cfgs.vedlegg.topic))
        .also { log.info("Created topic $it for ${cfgs.vedlegg}") }

    private fun createSubscription() =
        SubscriptionAdminClient.create().createSubscription(SubscriptionName.of(cfgs.id, cfgs.vedlegg.subscription),
                TopicName.of(cfgs.id, cfgs.vedlegg.topic),
                getDefaultInstance(),
                10).also {
            log.info("Created pull subscription $it for ${cfgs.vedlegg}")
        }

    private fun hasSubscription() =
        SubscriptionAdminClient.create().listSubscriptions(cfgs.id).iterateAll().map { it -> it.name }
            .contains(cfgs.vedlegg.subscription)

    private fun hasTopic() =
        TopicAdminClient.create().listTopics(cfgs.id).iterateAll().map { it -> it.name }
            .contains(cfgs.vedlegg.topic)

    private fun subscribe() {
        val subscriptionName = ProjectSubscriptionName.of(cfgs.id, cfgs.vedlegg.subscription)
        val receiver = MessageReceiver { message, consumer ->
            log.info("Id: ${message.messageId}")
            log.info("Data: ${message.attributesMap}")
            consumer.ack()
        }
        Subscriber.newBuilder(subscriptionName, receiver).build().apply {
            startAsync().awaitRunning()
            awaitRunning()
        }
    }
}