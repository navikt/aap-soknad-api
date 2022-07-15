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
import org.springframework.boot.actuate.endpoint.annotation.Selector
import org.springframework.stereotype.Component

@Component
class PubSubIAC(private val cfgs: BucketsConfig, private val storage: Storage) : InitializingBean {

    private val log = LoggerUtil.getLogger(javaClass)
    override fun afterPropertiesSet() {
        init()
    }

    private fun init() {
        if (!harTopic()) {
            lagTopic()
        }
        else {
            log.trace("Topics ${cfgs.topicName} finnes allerede i ${cfgs.projectName}")
        }
        if (!harSubscription()) {
            lagSubscription()
        }
        else {
            log.trace("Subscription ${cfgs.subscriptionName} finnes allerede for topic ${cfgs.topicName}")
        }

        setPubSubAdminPolicyForBucketServiceAccountOnTopic(cfgs.topicFullName)  //Idempotent

        if (!harNotifikasjon()) {
            lagNotifikasjon()
        }
        else {
            log.trace("${cfgs.mellom.navn} har allerede en notifikasjon på ${cfgs.topicName}")
        }
    }

    private fun harTopic() =
        listTopics()
            .map { it.substringAfterLast('/') }
            .contains(cfgs.mellom.subscription.topic)

    private fun lagTopic() =
        TopicAdminClient.create().use { c ->
            c.createTopic(cfgs.topicName).also {
                log.trace("Lagd topic ${it.name}")
            }
        }

    private fun harNotifikasjon() =
        cfgs.mellom.subscription.topic == listNotifikasjoner()
            .map { it.substringAfterLast('/') }
            .firstOrNull()

    private fun lagNotifikasjon() =
        storage.createNotification(cfgs.mellom.navn,
                NotificationInfo.newBuilder(cfgs.topicFullName)
                    .setEventTypes(OBJECT_FINALIZE, OBJECT_DELETE)
                    .setPayloadFormat(JSON_API_V1)
                    .build()).also {
            log.trace("Lagd notifikasjon ${it.notificationId} for topic ${cfgs.topicFullName}")
        }

    private fun setPubSubAdminPolicyForBucketServiceAccountOnTopic(topic: String) =
        TopicAdminClient.create().use { c ->
            with(topic) {
                c.setIamPolicy(SetIamPolicyRequest.newBuilder()
                    .setResource(this)
                    .setPolicy(Policy.newBuilder(c.getIamPolicy(GetIamPolicyRequest.newBuilder()
                        .setResource(this).build())).addBindings(Binding.newBuilder()
                        .setRole("roles/pubsub.publisher")
                        .setRole("roles/cloudkms.cryptoKeyEncrypterDecrypter")
                        .addMembers("serviceAccount:${storage.getServiceAccount(cfgs.id).email}")
                        .build()).build())
                    .build()).also { log.trace("Ny policy er ${it.bindingsList}") }
            }
        }

    private fun listNellomlagerNotifikasjoner() = listNotifikasjoner()

    private fun listNotifikasjoner() =
        storage.listNotifications(cfgs.mellom.navn)
            .map { it.topic }

    fun listTopics() =
        TopicAdminClient.create().use { c ->
            c.listTopics(cfgs.projectName)
                .iterateAll()
                .map { it.name }
        }

    private fun listSubscriptions() =
        TopicAdminClient.create().use { c ->
            c.listTopicSubscriptions(cfgs.topicName)
                .iterateAll()
        }

    private fun harSubscription() =
        listSubscriptions()
            .map { it.substringAfterLast('/') }
            .contains(cfgs.mellom.subscription.navn)

    private fun lagSubscription() =
        SubscriptionAdminClient.create().use { c ->
            c.createSubscription(cfgs.subscriptionName,
                    cfgs.topicName,
                    PushConfig.getDefaultInstance(),
                    10).also {
                log.trace("Lagd pull subscription ${it.name}")
            }
        }

    @Component
    @Endpoint(id = "iac")
    class IACEndpoint(private val iac: PubSubIAC) {
        @ReadOperation
        fun iacOperation() =
            with(iac) {
                mapOf("topics" to listTopics(),
                        "subscriptions" to listSubscriptions(),
                        "notifications" to listNellomlagerNotifikasjoner())
            }

        @ReadOperation
        fun iacByName(@Selector name: String) =
            with(iac) {
                when (name) {
                    "topics" -> mapOf("topics" to listTopics())
                    "subscriptions" -> mapOf("subscriptions" to listSubscriptions())
                    "notifications" -> mapOf("notifications" to listNellomlagerNotifikasjoner())
                    else -> iacOperation()
                }
            }
    }
}