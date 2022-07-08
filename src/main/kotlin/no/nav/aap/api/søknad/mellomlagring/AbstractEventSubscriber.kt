package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
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
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PushConfig.getDefaultInstance
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.BucketCfg
import no.nav.aap.util.LoggerUtil

abstract class AbstractEventSubscriber(protected val mapper: ObjectMapper,
                                       protected val dittNav: DittNavClient,
                                       private val storage: Storage,
                                       private val cfg: BucketCfg,
                                       private val projectId: String) {

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
                log.trace("Topic $topic finnes allerede i $projectId")
            }
            if (!harSubscription()) {
                lagSubscription()
            }
            else {
                log.trace("Subscription $subscription finnes allerede for $topic")
            }

            setPolicy()  //Idempotent

            if (!harNotifikasjon()) {
                lagNotifikasjon()
            }
            else {
                log.trace("$navn har allerede en notifikasjon på $topic")
            }
        }

    private fun abonner() =
        Subscriber.newBuilder(ProjectSubscriptionName.of(projectId, cfg.subscription), receiver()).build().apply {
            startAsync().awaitRunning()
            awaitRunning() // TODO sjekk dette
                .also {
                    log.trace("Abonnerert på events  via subscriber $this.'")
                }
        }

    private fun harTopic() =
        TopicAdminClient.create().use { client ->
            client.listTopics(ProjectName.of(projectId))
                .iterateAll()
                .map { it.name }
                .map { it.substringAfterLast('/') }
                .contains(cfg.topic)
        }

    private fun lagTopic() =
        TopicAdminClient.create().use { client ->
            client.createTopic(TopicName.of(projectId, cfg.topic)).also {
                log.trace("Lagd topic ${it.name}")
            }
        }

    private fun harNotifikasjon() =
        with(cfg) {
            topic == storage.listNotifications(navn)
                .map { it.topic }
                .map { it.substringAfterLast('/') }
                .firstOrNull()
        }

    private fun lagNotifikasjon() =
        with(cfg) {
            storage.createNotification(navn,
                    NotificationInfo.newBuilder(TopicName.of(projectId, topic).toString())
                        .setEventTypes(OBJECT_FINALIZE, OBJECT_DELETE)
                        .setPayloadFormat(JSON_API_V1)
                        .build()).also {
                log.trace("Lagd notifikasjon ${it.notificationId} for topic ${it.topic}")
            }
        }

    private fun setPolicy() =
        TopicAdminClient.create().use { client ->
            with(TopicName.of(projectId, cfg.topic)) {
                client.setIamPolicy(SetIamPolicyRequest.newBuilder()
                    .setResource(this.toString())
                    .setPolicy(Policy.newBuilder(client.getIamPolicy(GetIamPolicyRequest.newBuilder()
                        .setResource(this.toString()).build())).addBindings(Binding.newBuilder()
                        .setRole("roles/pubsub.publisher")
                        .addMembers("serviceAccount:${storage.getServiceAccount(projectId).email}")
                        .build()).build())
                    .build()).also { log.trace("Ny policy er $it") }
            }
        }

    private fun harSubscription() =
        with(cfg) {
            TopicAdminClient.create().use { client ->
                client.listTopicSubscriptions(TopicName.of(projectId, topic))
                    .iterateAll()
                    .map { it.substringAfterLast('/') }
                    .contains(subscription)
            }
        }

    private fun lagSubscription() =
        with(cfg) {
            SubscriptionAdminClient.create().use { client ->
                client.createSubscription(SubscriptionName.of(projectId, subscription),
                        TopicName.of(projectId, topic),
                        getDefaultInstance(),
                        10).also {
                    log.trace("Lagd pull subscription ${it.name}")
                }
            }
        }

    companion object {
        const val EVENT_TYPE = "eventType"
        const val OVERWROTEGENERATION = "overwroteGeneration"
        const val OVERWRITTEBBYGENERATION = "overwrittenByGeneration"
        const val SKJEMATYPE = "skjemaType"
        const val UUID_ = "uuid"
        const val METADATA = "metadata"
    }
}