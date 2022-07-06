package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.storage.NotificationInfo
import com.google.cloud.storage.NotificationInfo.EventType
import com.google.cloud.storage.NotificationInfo.PayloadFormat.JSON_API_V1
import com.google.cloud.storage.Storage
import com.google.iam.v1.Binding
import com.google.iam.v1.GetIamPolicyRequest
import com.google.iam.v1.Policy
import com.google.iam.v1.SetIamPolicyRequest
import com.google.pubsub.v1.ProjectName
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PushConfig.getDefaultInstance
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.BucketCfg
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

@Component
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
                                       private val cfg: BucketCfg,
                                       private val id: String) {

    protected val log = LoggerUtil.getLogger(javaClass)
    abstract fun receiver(): MessageReceiver

    init {
        with(cfg) {

            log.info("Abonnerer på events")
            if (!hasTopic()) {
                createTopic().also {
                    log.info("Lagd topic $it")
                }
            }
            else {
                log.info("Topic $topic finnes allerede i  $id")
            }
            if (!hasSubscriptionOnTopic()) {
                createSubscription().also {
                    log.info("Lagd subscription $it")
                }
            }
            else {
                log.info("Subscription $subscription finnes allerede for $topic")
            }

            setPolicyForServiceAccount().also {
                log.info("Satt policy pubsub.publisher på topic  $topic for ${storage.getServiceAccount(id).email} OK")
            }
            if (!hasNotification()) {
                createNotification().also {
                    log.info("Lagd notifikasjon $it for topic $navn")
                }
            }
            else {
                log.info("$navn har allerede en notifikasjon på $topic")

            }
            subscribe().also {
                log.info("Abonnerert på events  via subscription $subscription")
            }
        }
    }

    private fun subscribe() =
        Subscriber.newBuilder(ProjectSubscriptionName.of(id, cfg.subscription), receiver()).build().apply {
            startAsync().awaitRunning()
            awaitRunning()  // TODO sjekk dette
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
        with(cfg) {
            topic == storage.listNotifications(navn)
                .map { it.topic }
                .map { it.substringAfterLast('/') }
                .firstOrNull()
        }

    private fun createTopic() =
        TopicAdminClient.create().use { client ->
            client.createTopic(TopicName.of(id, cfg.topic))
        }

    private fun createSubscription() =
        with(cfg) {
            SubscriptionAdminClient.create().use { client ->
                client.createSubscription(SubscriptionName.of(id, subscription),
                        TopicName.of(id, topic),
                        getDefaultInstance(),
                        10).also {
                    log.info("Lagd pull subscription $it for $cfg")
                }
            }
        }

    private fun hasSubscriptionOnTopic() =
        with(cfg) {
            TopicAdminClient.create().use { client ->
                client.listTopicSubscriptions(TopicName.of(id, topic))
                    .iterateAll()
                    .map { it.substringAfterLast('/') }
                    .contains(subscription)
            }
        }

    private fun hasTopic() =
        TopicAdminClient.create().use { client ->
            client.listTopics(ProjectName.of(id))
                .iterateAll()
                .map { it.name }
                .map { it.substringAfterLast('/') }
                .contains(cfg.topic)
        }

    fun setPolicyForServiceAccount() =
        TopicAdminClient.create().use { client ->
            with(TopicName.of(id, cfg.topic)) {
                client.setIamPolicy(SetIamPolicyRequest.newBuilder()
                    .setResource(this.toString())
                    .setPolicy(Policy.newBuilder(client.getIamPolicy(GetIamPolicyRequest.newBuilder()
                        .setResource(this.toString()).build())).addBindings(Binding.newBuilder()
                        .setRole("roles/pubsub.publisher")
                        .addMembers("serviceAccount:${storage.getServiceAccount(id).email}")
                        .build()).build())
                    .build()).also { log.info("Ny policy er$it") }
            }
        }
}