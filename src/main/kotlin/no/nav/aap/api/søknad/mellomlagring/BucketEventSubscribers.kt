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
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.MellomBucketCfg
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class VedleggEventSubscriber(private val storage: Storage, private val cfgs: BucketsConfig) :
    AbstractEventSubscriber(storage, cfgs.vedlegg, cfgs.id) {

    override fun receiver() =
        MessageReceiver { message, consumer ->
            log.info("Id: ${message.messageId}")
            log.info("Data: ${message.attributesMap}")
            consumer.ack()
        }
}

//@Component
class MellomlagringEventSubscriber(private val storage: Storage, private val cfgs: BucketsConfig) :
    AbstractEventSubscriber(storage, cfgs.mellom, cfgs.id) {

    override fun receiver() =
        MessageReceiver { message, consumer ->
            log.info("Id: ${message.messageId}")
            log.info("Data: ${message.attributesMap}")
            consumer.ack()
        }
}

abstract class AbstractEventSubscriber(private val storage: Storage,
                                       private val cfg: MellomBucketCfg,
                                       private val id: String) {

    protected val log = LoggerUtil.getLogger(javaClass)
    abstract fun receiver(): MessageReceiver

    init {
        log.info("Abonnerer på events for $cfg")
        if (!hasTopic()) {
            createTopic().also {
                log.info("Lagd topic $it for $cfg")
            }
        }
        else {
            log.info("Topic ${cfg.topic} finnes allerede i prosjekt  $id")
        }
        if (!hasSubscriptionOnTopic()) {
            createSubscription().also {
                log.info("Lagd subscription $it for $cfg")
            }
        }
        else {
            log.info("Subscription ${cfg.subscription} finnes allerede for ${cfg.topic}")
        }
        if (!hasNotification()) {
            createNotification().also {
                log.info("Lagd notifikasjon $it for ${cfg.navn}")
            }
        }
        else {
            log.info("${cfg.navn} har allerede en notifikasjon på ${cfg.topic}")

        }
        subscribe().also {
            log.info("Abonnerert på events $it for $cfg via subscription ${cfg.subscription}")
        }
    }

    private fun subscribe() =
        Subscriber.newBuilder(ProjectSubscriptionName.of(id, cfg.subscription), receiver()).build().apply {
            startAsync().awaitRunning()
            awaitRunning()
        }

    private fun createNotification() =
        with(cfg) {
            storage.createNotification(navn,
                    NotificationInfo.newBuilder(TopicName.of(id, topic).toString())
                        .setEventTypes(*EventType.values())
                        .setPayloadFormat(JSON_API_V1)
                        .build());
        }

    private fun hasNotification() =
        cfg.topic == storage.listNotifications(cfg.navn)
            .map { it.topic }
            .map { it.substringAfterLast('/') }.firstOrNull()

    private fun createTopic() = TopicAdminClient.create().createTopic(TopicName.of(id, cfg.topic))

    private fun createSubscription() =
        with(cfg) {
            SubscriptionAdminClient.create().createSubscription(SubscriptionName.of(id, subscription),
                    TopicName.of(id, topic),
                    getDefaultInstance(),
                    10).also {
                log.info("Lagd pull subscription $it for $cfg")
            }
        }

    private fun hasSubscriptionOnTopic() =
        with(cfg) {
            TopicAdminClient.create().listTopicSubscriptions(TopicName.of(id, topic))
                .iterateAll()
                .map { it.substringAfterLast('/') }
                .contains(subscription)
        }

    private fun hasTopic() =
        TopicAdminClient.create().listTopics(ProjectName.of(id))
            .iterateAll()
            .map { it.name }
            .map { it.substringAfterLast('/') }
            .contains(cfg.topic)
}