package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.storage.NotificationInfo
import com.google.cloud.storage.NotificationInfo.EventType
import com.google.cloud.storage.NotificationInfo.PayloadFormat.JSON_API_V1
import com.google.cloud.storage.Storage
import com.google.pubsub.v1.ProjectName
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PushConfig.getDefaultInstance
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class BucketVedleggEventSubscriber(private val storage: Storage, private val cfgs: BucketsConfig) {
    private val log = LoggerUtil.getLogger(javaClass)

    init {
        log.info("Abonnerer på events for vedlegg")
        if (!hasTopic()) {
            createTopic().also {
                log.info("Created topic $it for ${cfgs.vedlegg}")
            }
        }
        else {
            log.info("Topic ${cfgs.vedlegg.topic} finnes allerede i ${cfgs.id}")
        }
        if (!hasSubscriptionOnTopic()) {
            createSubscription().also {
                log.info("Lagd subscription $it for ${cfgs.vedlegg}")
            }
        }
        else {
            log.info("Subscription ${cfgs.vedlegg.subscription} finnes allerede for ${cfgs.vedlegg.topic}")
        }
        if (!hasNotification()) {
            createNotification().also {
                log.info("Lagd notifikasjon $it for ${cfgs.vedlegg.navn}")
            }
        }
        else {
            log.info("${cfgs.vedlegg.navn} har allerede en notifikasjon på ${cfgs.vedlegg.topic}")
            createNotification() // TEST

        }
        subscribe().also {
            log.info("Abonnerert på events for vedlegg OK ${cfgs.vedlegg} via subscription ${cfgs.vedlegg.subscription}")
        }
    }

    fun createNotification() {
        val notificationInfo = NotificationInfo.newBuilder(TopicName.of(cfgs.id, cfgs.vedlegg.topic).topic)
            .setEventTypes(*EventType.values())
            .setPayloadFormat(JSON_API_V1)
            .build();
        log.info("Notification info $notificationInfo")
        // val notification = storage.createNotification(cfgs.vedlegg.navn, notificationInfo);
    }

    fun hasNotification() =
        cfgs.vedlegg.topic == storage.listNotifications(cfgs.vedlegg.navn).map { it.topic }
            .map { it -> it.substringAfterLast('/') }.firstOrNull()

    private fun createTopic() = TopicAdminClient.create().createTopic(TopicName.of(cfgs.id, cfgs.vedlegg.topic))

    private fun createSubscription() =
        SubscriptionAdminClient.create().createSubscription(SubscriptionName.of(cfgs.id, cfgs.vedlegg.subscription),
                TopicName.of(cfgs.id, cfgs.vedlegg.topic),
                getDefaultInstance(),
                10).also {
            log.info("Created pull subscription $it for ${cfgs.vedlegg}")
        }

    private fun hasSubscriptionOnTopic(): Boolean {
        val subs =
            TopicAdminClient.create().listTopicSubscriptions(TopicName.of(cfgs.id, cfgs.vedlegg.topic)).iterateAll()
                .map { it.substringAfterLast('/') }
                .toList()
        log.info("Sjekker $subs mot ${cfgs.vedlegg.subscription}")
        return subs.contains(cfgs.vedlegg.subscription)
    }

    private fun hasTopic(): Boolean {
        val topics = TopicAdminClient.create().listTopics(ProjectName.of(cfgs.id)).iterateAll().map { it.name }
            .map { it.substringAfterLast('/') }
        log.info("Sjekker $topics mot ${cfgs.vedlegg.topic}")
        return topics.contains(cfgs.vedlegg.topic)
    }

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