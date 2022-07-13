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
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.BucketCfg
import no.nav.aap.util.LoggerUtil
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.stereotype.Component

@Component
class PubSubIACBean(private val cfgs: BucketsConfig, private val storage: Storage) : InitializingBean {

    private val log = LoggerUtil.getLogger(javaClass)
    override fun afterPropertiesSet() {
        init(cfgs.mellom)
    }

    private fun init(cfg: BucketCfg) =
        with(cfg) {
            if (!harTopic(cfg)) {
                lagTopic(cfg)
            }
            else {
                log.trace("Topic $topic (${topicName(cfg)}) finnes allerede i ${cfgs.id}")
            }
            if (!harSubscription(cfg)) {
                lagSubscription(cfg)
            }
            else {
                log.trace("Subscription $subscription (${subscriptionName(cfg)}) finnes allerede for $topic (${
                    topicName(cfg)
                }")
            }

            setPubSubAdminPolicyForBucketServiceAccountOnTopic(topicFullName(cfg))  //Idempotent

            if (!harNotifikasjon(cfg)) {
                lagNotifikasjon(cfg)
            }
            else {
                log.trace("$navn har allerede en notifikasjon på $topic (${topicName(cfg)})")
            }
        }

    private fun harTopic(cfg: BucketCfg) =
        TopicAdminClient.create().use { client ->
            client.listTopics(projectName())
                .iterateAll()
                .map { it.name }
                .map { it.substringAfterLast('/') }
                .contains(cfg.topic)
        }

    private fun lagTopic(cfg: BucketCfg) =
        TopicAdminClient.create().use { client ->
            client.createTopic(topicName(cfg)).also {
                log.trace("Lagd topic ${it.name}")
            }
        }

    private fun harNotifikasjon(cfg: BucketCfg) =
        cfg.topic == storage.listNotifications(cfg.navn)
            .map { it.topic }
            .map { it.substringAfterLast('/') }
            .firstOrNull()

    private fun lagNotifikasjon(cfg: BucketCfg) =
        storage.createNotification(cfg.navn,
                NotificationInfo.newBuilder(topicFullName(cfg))
                    .setEventTypes(OBJECT_FINALIZE, OBJECT_DELETE)
                    .setPayloadFormat(JSON_API_V1)
                    .build()).also {
            log.trace("Lagd notifikasjon ${it.notificationId} for topic ${it.topic} (${topicName(cfg)})")
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

    private fun listNotifikasjoner(cfg: BucketCfg) =
        storage.listNotifications(cfg.navn)
            .map { it.topic }

    private fun listMellomlagerTopics() = listTopics(cfgs.mellom)

    fun listTopics(cfg: BucketCfg) =
        TopicAdminClient.create().use { client ->
            client.listTopics(projectName())
                .iterateAll()
                .map { it.name }
        }

    private fun listMellomlagerSubscriptions() = listSubscriptions(cfgs.mellom)

    private fun listSubscriptions(cfg: BucketCfg) =
        TopicAdminClient.create().use { client ->
            client.listTopicSubscriptions(topicName(cfg))
                .iterateAll()
        }

    private fun harSubscription(cfg: BucketCfg) =
        TopicAdminClient.create().use { client ->
            client.listTopicSubscriptions(topicName(cfg))
                .iterateAll()
                .map { it.substringAfterLast('/') }
                .contains(cfg.subscription)
        }

    private fun lagSubscription(cfg: BucketCfg) =
        SubscriptionAdminClient.create().use { client ->
            client.createSubscription(subscriptionName(cfg),
                    topicName(cfg),
                    PushConfig.getDefaultInstance(),
                    10).also {
                log.trace("Lagd pull subscription ${it.name}")
            }
        }

    private fun subscriptionName(cfg: BucketCfg) = SubscriptionName.of(cfgs.id, cfg.subscription)
    private fun projectName() = ProjectName.of(cfgs.id)
    private fun topicName(cfg: BucketCfg) = TopicName.of(cfgs.id, cfg.topic)
    private fun topicFullName(cfg: BucketCfg) = topicName(cfg).toString()

    @Component
    @Endpoint(id = "iac")
    class IACEndpoint(private val iac: PubSubIACBean) {
        @ReadOperation
        fun iacOpeeration() =
            mapOf("topics" to iac.listMellomlagerTopics(),
                    "subscriptions" to iac.listMellomlagerSubscriptions(),
                    "notifications" to iac.listNellomlagerotifikasjoner())

        @ReadOperation
        fun customEndPointByName(@Selector name: String) = "iac"
    }
}