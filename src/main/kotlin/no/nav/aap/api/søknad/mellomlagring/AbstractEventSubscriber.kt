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
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.BucketCfg
import no.nav.aap.util.LoggerUtil

abstract class AbstractEventSubscriber(private val storage: Storage,
                                       private val cfg: BucketCfg,
                                       private val id: String) {

    protected val log = LoggerUtil.getLogger(javaClass)
    abstract fun receiver(): MessageReceiver

    init {
        init(cfg)
        abonner()
    }

    private fun init(cfg: BucketCfg) =
        with(cfg) {
            if (!harTopic()) {
                lagTopic()
            }
            else {
                log.info("Topic $topic finnes allerede i $id")
            }
            if (!harSubscription()) {
                lagSubscription()
            }
            else {
                log.info("Subscription $subscription finnes allerede for $topic")
            }

            setPolicy()  //Idempotent

            if (!harNotifikasjon()) {
                lagNotifikasjon()
            }
            else {
                log.info("$navn har allerede en notifikasjon på $topic")
            }
        }

    private fun abonner() =
        Subscriber.newBuilder(ProjectSubscriptionName.of(id, cfg.subscription), receiver()).build().apply {
            startAsync().awaitRunning()
            awaitRunning() // TODO sjekk dette
                .also {
                    log.info("Abonnerert på events  via subscriber $this.'")
                }
        }

    private fun lagNotifikasjon() =
        with(cfg) {
            storage.createNotification(navn,
                    NotificationInfo.newBuilder(TopicName.of(id, topic).toString())
                        .setEventTypes(*EventType.values())
                        .setPayloadFormat(JSON_API_V1)
                        .build()).also {
                log.info("Lagd notifikasjon ${it.notificationId} for topic ${it.topic}")
            }
        }

    private fun harNotifikasjon() =
        with(cfg) {
            topic == storage.listNotifications(navn)
                .map { it.topic }
                .map { it.substringAfterLast('/') }
                .firstOrNull()
        }

    private fun lagTopic() =
        TopicAdminClient.create().use { client ->
            client.createTopic(TopicName.of(id, cfg.topic)).also {
                log.info("Lagd topic ${it.name}")
            }
        }

    private fun lagSubscription() =
        with(cfg) {
            SubscriptionAdminClient.create().use { client ->
                client.createSubscription(SubscriptionName.of(id, subscription),
                        TopicName.of(id, topic),
                        PushConfig.getDefaultInstance(),
                        10).also {
                    log.info("Lagd pull subscription ${it.name}")
                }
            }
        }

    private fun harSubscription() =
        with(cfg) {
            TopicAdminClient.create().use { client ->
                client.listTopicSubscriptions(TopicName.of(id, topic))
                    .iterateAll()
                    .map { it.substringAfterLast('/') }
                    .contains(subscription)
            }
        }

    private fun harTopic() =
        TopicAdminClient.create().use { client ->
            client.listTopics(ProjectName.of(id))
                .iterateAll()
                .map { it.name }
                .map { it.substringAfterLast('/') }
                .contains(cfg.topic)
        }

    private fun setPolicy() =
        TopicAdminClient.create().use { client ->
            with(TopicName.of(id, cfg.topic)) {
                client.setIamPolicy(SetIamPolicyRequest.newBuilder()
                    .setResource(this.toString())
                    .setPolicy(Policy.newBuilder(client.getIamPolicy(GetIamPolicyRequest.newBuilder()
                        .setResource(this.toString()).build())).addBindings(Binding.newBuilder()
                        .setRole("roles/pubsub.publisher")
                        .addMembers("serviceAccount:${storage.getServiceAccount(id).email}")
                        .build()).build())
                    .build()).also { log.info("Ny policy er $it") }
            }
        }
}