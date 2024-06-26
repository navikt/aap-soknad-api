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
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.stereotype.Component
import no.nav.aap.api.søknad.mellomlagring.BucketConfig
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.MellomlagringBucketConfig.SubscriptionConfig
import no.nav.aap.util.LoggerUtil.getLogger

@Component
class PubSubIAC(private val cfg: BucketConfig, private val storage: Storage, private val admin: PubSubAdmin) : CommandLineRunner {

    override fun run(vararg args: String?) {
        with(cfg.mellom.subscription) {
            log.info("IAC Pub sub init")
            if (!harTopic(topic)) {
                lagTopic(topic)
            }
            else {
                log.info("Topic $topic finnes allerede i ${cfg.project}")
            }
            if (!harSubscription(navn)) {
                lagSubscription(this)
            }
            else {
                log.info("Subscription $navn finnes allerede for topic $topic")
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
            log.info("Lagd topic ${it.name}")
        }

    private fun harNotifikasjon(topic: String) =
        storage.listNotifications(cfg.mellom.navn)
            .any { it.topic.substringAfterLast('/') == topic }

    private fun lagNotifikasjon(navn: String) =
        with(cfg) {
            val topic = TopicName.of(project, navn).toString()
            log.info("Lager en notifikasjon på topic $topic")
            storage.createNotification(mellom.navn,
                notificationInfo(topic)).also {
                log.trace("Lagd notifikasjon ${it.notificationId} for topic $topic")
            }
        }

    private fun notificationInfo(topic : String) : NotificationInfo? = NotificationInfo.newBuilder(topic)
        .setEventTypes(OBJECT_FINALIZE, OBJECT_DELETE)
        .setPayloadFormat(JSON_API_V1)
        .build()

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
                    .build()).also { log.trace("Policy er {}", it.bindingsList) }
            }
        }

    private fun harSubscription(navn: String) =
        admin.getSubscription(navn) != null

    private fun lagSubscription(cfg: SubscriptionConfig) =
        with(cfg) {
            log.info("Lager pull subscription $navn for $topic")
            admin.createSubscription(navn, topic)
                .also {
                    log.info("Lagd subscription ${it.name}")
                }
        }

    @Component
    @Endpoint(id = "iac")
    class IACEndpoint(private val storage: Storage, private val cfg: BucketConfig) {
        @ReadOperation
        fun iacOperation() =
            with(cfg) {
                mutableMapOf("bøtte" to mellom,
                        "notification" to storage.listNotifications(mellom.navn).map { it.topic },
                        "nøkkel" to kms.key,
                        "ring" to kms.ring)
            }
    }

    companion object {
        private val log = getLogger(PubSubIAC::class.java)
    }


}