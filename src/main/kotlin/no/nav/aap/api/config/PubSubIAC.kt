package no.nav.aap.api.config

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.spring.pubsub.PubSubAdmin
import com.google.cloud.storage.NotificationInfo
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_DELETE
import com.google.cloud.storage.NotificationInfo.EventType.OBJECT_FINALIZE
import com.google.cloud.storage.NotificationInfo.PayloadFormat.JSON_API_V1
import com.google.cloud.storage.Storage
import com.google.iam.v1.Binding
import com.google.iam.v1.GetIamPolicyRequest
import com.google.iam.v1.Policy
import com.google.iam.v1.SetIamPolicyRequest
import com.google.pubsub.v1.TopicName
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.MellomlagringBucketConfig.SubscriptionConfig
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.stereotype.Component

@Component
class PubSubIAC(private val cfg: BucketsConfig, private val storage: Storage, private val admin: PubSubAdmin) {
    init {
        with(cfg.mellom.subscription) {
            if (!harTopic(topic)) {
                lagTopic(topic)
            }
            else {
                log.trace("Topic $topic finnes allerede i ${cfg.project}")
            }
            if (!harSubscription(navn)) {
                lagSubscription(this)
            }
            else {
                log.trace("Subscription $navn finnes allerede for topic $topic")
            }

            if (!harNotifikasjon(topic)) {
                setPubSubAdminPolicyForBucketServiceAccountOnTopic(cfg.project, topic)
                lagNotifikasjon(topic)
            }
            else {
                log.trace("${cfg.mellom.navn} har allerede en notifikasjon på $topic")
            }
        }
    }

    private fun harTopic(topic: String) =
        admin.getTopic(topic) != null

    private fun lagTopic(topic: String) =
        admin.createTopic(topic).also {
            log.trace("Lagd topic ${it.name}")
        }

    private fun harNotifikasjon(topic: String) =
        storage.listNotifications(cfg.mellom.navn)
            .map { it.topic }
            .find { it.substringAfterLast('/') == topic } != null

    private fun lagNotifikasjon(navn: String) =
        with(cfg) {
            val topic = TopicName.of(project, navn).toString()
            log.info("Lager en notifikasjon på topic $topic")
            storage.createNotification(mellom.navn,
                    NotificationInfo.newBuilder(topic)
                        .setEventTypes(OBJECT_FINALIZE, OBJECT_DELETE)
                        .setPayloadFormat(JSON_API_V1)
                        .build()).also {
                log.trace("Lagd notifikasjon ${it.notificationId} for topic $topic")
            }
        }

    private fun setPubSubAdminPolicyForBucketServiceAccountOnTopic(project: String, topic: String) =
        TopicAdminClient.create().use { c ->
            with(TopicName.of(project, topic).toString()) {
                log.info("Setter policy pubsub.publisher for $this")
                c.setIamPolicy(SetIamPolicyRequest.newBuilder()
                    .setResource(this)
                    .setPolicy(Policy.newBuilder(c.getIamPolicy(GetIamPolicyRequest.newBuilder()
                        .setResource(this).build())).addBindings(Binding.newBuilder()
                        .setRole("roles/pubsub.publisher")
                        .addMembers("serviceAccount:${storage.getServiceAccount(project).email}")
                        .build()).build())
                    .build()).also { log.trace("Policy er ${it.bindingsList}") }
            }
        }

    private fun harSubscription(navn: String) =
        admin.getSubscription(navn) != null

    private fun lagSubscription(cfg: SubscriptionConfig) =
        with(cfg) {
            log.info("Lager pull subscription $navn for $topic")
            admin.createSubscription(navn, topic)
                .also {
                    log.trace("Lagd pull subscription ${it.name}")
                }
        }

    @Component
    @Endpoint(id = "iac")
    class IACEndpoint(private val storage: Storage, private val cfg: BucketsConfig) {
        @ReadOperation
        fun iacOperation() =
            with(cfg) {
                mutableMapOf("bøtte" to mellom,
                        "notification" to storage.listNotifications(mellom.navn).map { it -> it.topic },
                        "nøkkel" to kms.key,
                        "ring" to kms.ring)
            }
    }

    companion object {
        private val log = getLogger(PubSubIAC::class.java)
    }
}