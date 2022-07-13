package no.nav.aap.api.config

import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.storage.NotificationInfo
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import com.google.cloud.storage.NotificationInfo.PayloadFormat.JSON_API_V1
import com.google.cloud.storage.Storage
import com.google.iam.v1.Binding
import com.google.iam.v1.GetIamPolicyRequest
import com.google.iam.v1.Policy
import com.google.iam.v1.SetIamPolicyRequest
import com.google.pubsub.v1.ProjectName
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.MellomlagringBucketConfig
import no.nav.aap.util.LoggerUtil
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.stereotype.Component

@Component
class PubSubIAC(private val cfgs: BucketsConfig, private val storage: Storage) : InitializingBean {

    private val log = LoggerUtil.getLogger(javaClass)
    override fun afterPropertiesSet() {
        init(cfgs.mellom)
    }

    private fun init(cfg: MellomlagringBucketConfig) =
        with(cfg) {
            if (!harTopic(this)) {
                lagTopic(this)
            }
            else {
                log.trace("Topic (${topicName(this)}) finnes allerede i ${projectName()}")
            }
            if (!harSubscription(this)) {
                lagSubscription(this)
            }
            else {
                log.trace("Subscription (${subscriptionName(this)}) finnes allerede for topic (${
                    topicName(cfg)
                }")
            }

            setPubSubAdminPolicyForBucketServiceAccountOnTopic(topicFullName(this))  //Idempotent

            if (!harNotifikasjon(this)) {
                lagNotifikasjon(this)
            }
            else {
                log.trace("$navn har allerede en notifikasjon på (${topicName(this)})")
            }
        }

    private fun harTopic(cfg: MellomlagringBucketConfig) =
        TopicAdminClient.create().use { client ->
            client.listTopics(projectName())
                .iterateAll()
                .map { it.name }
                .map { it.substringAfterLast('/') }
                .contains(cfg.subscription.topic)
        }

    private fun lagTopic(cfg: MellomlagringBucketConfig) =
        TopicAdminClient.create().use { client ->
            client.createTopic(topicName(cfg)).also {
                log.trace("Lagd topic ${it.name}")
            }
        }

    private fun harNotifikasjon(cfg: MellomlagringBucketConfig) =
        cfg.subscription.topic == storage.listNotifications(cfg.navn)
            .map { it.topic }
            .map { it.substringAfterLast('/') }
            .firstOrNull()

    private fun lagNotifikasjon(cfg: MellomlagringBucketConfig) =
        storage.createNotification(cfg.navn,
                NotificationInfo.newBuilder(topicFullName(cfg))
                    .setEventTypes(OBJECT_FINALIZE, OBJECT_DELETE)
                    .setPayloadFormat(JSON_API_V1)
                    .build()).also {
            log.trace("Lagd notifikasjon ${it.notificationId} for topic (${topicName(cfg)})")
        }

    private fun setPubSubAdminPolicyForBucketServiceAccountOnTopic(topic: String) =
        TopicAdminClient.create().use { client ->
            with(topic) {
                client.setIamPolicy(SetIamPolicyRequest.newBuilder()
                    .setResource(this)
                    .setPolicy(Policy.newBuilder(client.getIamPolicy(GetIamPolicyRequest.newBuilder()
                        .setResource(this).build())).addBindings(Binding.newBuilder()
                        .setRole("roles/pubsub.publisher")
                        .addMembers("serviceAccount:${storage.getServiceAccount(cfgs.id).email}")
                        .build()).build())
                    .build()).also { log.trace("Ny policy er $it") }
            }
        }

    private fun listNellomlagerotifikasjoner() = listNotifikasjoner(cfgs.mellom)

    private fun listNotifikasjoner(cfg: MellomlagringBucketConfig) =
        storage.listNotifications(cfg.navn)
            .map { it.topic }

    private fun listMellomlagerTopics() = listTopics(cfgs.mellom)

    fun listTopics(cfg: MellomlagringBucketConfig) =
        TopicAdminClient.create().use { client ->
            client.listTopics(projectName())
                .iterateAll()
                .map { it.name }
        }

    private fun listMellomlagerSubscriptions() = listSubscriptions(cfgs.mellom)

    private fun listSubscriptions(cfg: MellomlagringBucketConfig) =
        TopicAdminClient.create().use { client ->
            client.listTopicSubscriptions(topicName(cfg))
                .iterateAll()
        }

    private fun harSubscription(cfg: MellomlagringBucketConfig) =
        TopicAdminClient.create().use { client ->
            client.listTopicSubscriptions(topicName(cfg))
                .iterateAll()
                .map { it.substringAfterLast('/') }
                .contains(cfg.subscription.navn)
        }

    private fun lagSubscription(cfg: MellomlagringBucketConfig) =
        SubscriptionAdminClient.create().use { client ->
            client.createSubscription(subscriptionName(cfg),
                    topicName(cfg),
                    PushConfig.getDefaultInstance(),
                    10).also {
                log.trace("Lagd pull subscription ${it.name}")
            }
        }

    private fun subscriptionName(cfg: MellomlagringBucketConfig) = SubscriptionName.of(cfgs.id, cfg.subscription.navn)
    private fun projectName() = ProjectName.of(cfgs.id)
    private fun topicName(cfg: MellomlagringBucketConfig) = TopicName.of(cfgs.id, cfg.subscription.topic)
    private fun topicFullName(cfg: MellomlagringBucketConfig) = topicName(cfg).toString()

    @Component
    @Endpoint(id = "iac")
    class IACEndpoint(private val iac: PubSubIAC) {
        @ReadOperation
        fun iacOpeeration() =
            mapOf("topics" to iac.listMellomlagerTopics(),
                    "subscriptions" to iac.listMellomlagerSubscriptions(),
                    "notifications" to iac.listNellomlagerotifikasjoner())

        @ReadOperation
        fun customEndPointByName(@Selector name: String) = "iac"
    }
}