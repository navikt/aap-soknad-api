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
                log.trace("Topic $topic finnes allerede i ${cfgs.id}")
            }
            if (!harSubscription(cfg)) {
                lagSubscription(cfg)
            }
            else {
                log.trace("Subscription $subscription finnes allerede for $topic")
            }

            setPolicy(cfg)  //Idempotent

            if (!harNotifikasjon(cfg)) {
                lagNotifikasjon(cfg)
            }
            else {
                log.trace("$navn har allerede en notifikasjon på $topic")
            }
        }

    private fun harTopic(cfg: BucketCfg) =
        TopicAdminClient.create().use { client ->
            client.listTopics(ProjectName.of(cfgs.id))
                .iterateAll()
                .map { it.name }
                .map { it.substringAfterLast('/') }
                .contains(cfg.topic)
        }

    private fun lagTopic(cfg: BucketCfg) =
        TopicAdminClient.create().use { client ->
            client.createTopic(TopicName.of(cfgs.id, cfg.topic)).also {
                log.trace("Lagd topic ${it.name}")
            }
        }

    private fun harNotifikasjon(cfg: BucketCfg) =
        with(cfg) {
            topic == storage.listNotifications(navn)
                .map { it.topic }
                .map { it.substringAfterLast('/') }
                .firstOrNull()
        }

    private fun lagNotifikasjon(cfg: BucketCfg) =
        with(cfg) {
            storage.createNotification(navn,
                    NotificationInfo.newBuilder(TopicName.of(cfgs.id, topic).toString())
                        .setEventTypes(OBJECT_FINALIZE, OBJECT_DELETE)
                        .setPayloadFormat(JSON_API_V1)
                        .build()).also {
                log.trace("Lagd notifikasjon ${it.notificationId} for topic ${it.topic}")
            }
        }

    private fun setPolicy(cfg: BucketCfg) =
        TopicAdminClient.create().use { client ->
            with(TopicName.of(cfgs.id, cfg.topic)) {
                client.setIamPolicy(SetIamPolicyRequest.newBuilder()
                    .setResource(this.toString())
                    .setPolicy(Policy.newBuilder(client.getIamPolicy(GetIamPolicyRequest.newBuilder()
                        .setResource(this.toString()).build())).addBindings(Binding.newBuilder()
                        .setRole("roles/pubsub.publisher")
                        .addMembers("serviceAccount:${storage.getServiceAccount(cfgs.id).email}")
                        .build()).build())
                    .build()).also { log.trace("Ny policy er $it") }
            }
        }

    private fun harSubscription(cfg: BucketCfg) =
        with(cfg) {
            TopicAdminClient.create().use { client ->
                client.listTopicSubscriptions(TopicName.of(cfgs.id, topic))
                    .iterateAll()
                    .map { it.substringAfterLast('/') }
                    .contains(subscription)
            }
        }

    private fun lagSubscription(cfg: BucketCfg) =
        with(cfg) {
            SubscriptionAdminClient.create().use { client ->
                client.createSubscription(SubscriptionName.of(cfgs.id, subscription),
                        TopicName.of(cfgs.id, topic),
                        PushConfig.getDefaultInstance(),
                        10).also {
                    log.trace("Lagd pull subscription ${it.name}")
                }
            }
        }

    @Component
    @Endpoint(id = "iac")
    class IACEndpoint {
        @ReadOperation
        fun iacOpeeration(): String {
            return "OK"
        }

        @ReadOperation
        fun customEndPointByName(@Selector name: String?): String {
            return "iac"
        }
    }
}