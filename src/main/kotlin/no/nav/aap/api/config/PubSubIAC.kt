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
import com.google.pubsub.v1.PushConfig
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig
import no.nav.aap.util.LoggerUtil
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.stereotype.Component

@Component
class PubSubIAC(private val cfgs: BucketsConfig, private val storage: Storage) : InitializingBean {

    private val log = LoggerUtil.getLogger(javaClass)
    override fun afterPropertiesSet() = init()

    private fun init() {
        with(cfgs) {
            if (!harTopic()) {
                lagTopic()
            }
            else {
                log.trace("Topics $topic finnes allerede i $project")
            }
            if (!harSubscription()) {
                lagSubscription()
            }
            else {
                log.trace("Subscription $subscription finnes allerede for topic $topic")
            }


            if (!harNotifikasjon()) {
                lagNotifikasjon()
            }
            else {
                log.trace("$mellomBøtte har allerede en notifikasjon på $topic")
            }
            setPubSubAdminPolicyForBucketServiceAccountOnTopic(topicNavn)  //Idempotent
        }
    }

    private fun harTopic() =
        listTopics()
            .contains(cfgs.topicNavn)

    private fun lagTopic() =
        TopicAdminClient.create().use { c ->
            c.createTopic(cfgs.topic).also {
                log.trace("Lagd topic ${it.name}")
            }
        }

    private fun harNotifikasjon() =
        cfgs.topicNavn == listTopicForNotifikasjon().substringAfterLast('/')

    private fun lagNotifikasjon() =
        with(cfgs) {
            storage.createNotification(mellomBøtte,
                    NotificationInfo.newBuilder(topicNavn)
                        .setEventTypes(OBJECT_FINALIZE, OBJECT_DELETE)
                        .setPayloadFormat(JSON_API_V1)
                        .build()).also {
                log.trace("Lagd notifikasjon ${it.notificationId} for topic $topicNavn")
            }
        }

    private fun setPubSubAdminPolicyForBucketServiceAccountOnTopic(topic: String) =
        TopicAdminClient.create().use { c ->
            with(topic) {
                c.setIamPolicy(SetIamPolicyRequest.newBuilder()
                    .setResource(this)
                    .setPolicy(Policy.newBuilder(c.getIamPolicy(GetIamPolicyRequest.newBuilder()
                        .setResource(this).build())).addBindings(Binding.newBuilder()
                        .setRole("roles/pubsub.publisher")
                        .addMembers("serviceAccount:${storage.getServiceAccount(cfgs.project.project).email}")
                        .build()).build())
                    .build()).also { log.trace("Policy på $topic er ${it.bindingsList}") }
            }
        }

    private fun listTopicForNotifikasjon() =
        with(cfgs) {
            storage.listNotifications(mellomBøtte)
                .map { it.topic }.first().also {
                    log.trace("X Topic er $it vs ${cfgs.topicNavn}")
                }
        }

    fun listTopics() =
        TopicAdminClient.create().use { c ->
            c.listTopics(cfgs.project)
                .iterateAll()
                .map { it.name }
        }

    private fun listSubscriptions() =
        TopicAdminClient.create().use { c ->
            c.listTopicSubscriptions(cfgs.topic)
                .iterateAll()
        }

    private fun harSubscription() =
        with(cfgs) {
            listSubscriptions()
                .map { it.substringAfterLast('/') }
                .contains(subscription.subscription)
        }

    private fun lagSubscription() =
        with(cfgs) {
            SubscriptionAdminClient.create().use { c ->
                c.createSubscription(subscription, topic,
                        PushConfig.getDefaultInstance(),
                        10).also {
                    log.trace("Lagd pull subscription ${it.name}")
                }
            }
        }

    @Component
    @Endpoint(id = "iac")
    class IACEndpoint(private val iac: PubSubIAC, private val cfgs: BucketsConfig) {
        @ReadOperation
        fun iacOperation() =
            with(cfgs) {
                mutableMapOf("bucket" to mellomBøtte,
                        "topic" to topicNavn,
                        "subscription" to subscription.toString(),
                        "notification" to iac.listTopicForNotifikasjon())
                    .apply {
                        putAll(mapOf("ring" to ringNavn,
                                "nøkkel" to nøkkelNavn))
                    }
            }
    }
}