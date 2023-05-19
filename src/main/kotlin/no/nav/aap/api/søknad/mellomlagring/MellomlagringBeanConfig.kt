package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.ServiceOptions
import com.google.cloud.spring.pubsub.core.PubSubTemplate
import com.google.cloud.spring.pubsub.integration.AckMode.MANUAL
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders.*
import com.google.cloud.storage.NotificationInfo.*
import com.google.cloud.storage.NotificationInfo.EventType.*
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler
import org.threeten.bp.Duration
import no.nav.aap.api.config.Metrikker
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.PubSubMessageExtensions.handle
import no.nav.aap.util.LoggerUtil

@Configuration(proxyBeanMethods = false)
class MellomlagringBeanConfig {
    private val log = LoggerUtil.getLogger(javaClass)


    @Bean
    @Primary
    fun gcpStorageRetrySettings(@Value("\${mellomlagring.timeout:2500}") timeoutMs: Int) =
        ServiceOptions.getDefaultRetrySettings().toBuilder()
            .setInitialRetryDelay(Duration.ofMillis(400))
            .setMaxRetryDelay(Duration.ofMillis(900))
            .setRetryDelayMultiplier(1.5)
            .setMaxAttempts(5)
            .setTotalTimeout(Duration.ofMillis(timeoutMs.toLong()))
            .build()
    @Bean
    @Primary
    fun gcpStorage(retrySettings: RetrySettings) =  StorageOptions
        .newBuilder()
        .setRetrySettings(retrySettings)
        .build()
        .service

    @Bean
    @Qualifier("storageInputChannel")
    fun pubsubInputChannel() = DirectChannel()

    @Bean
    fun storageChannelAdapter(cfg: BucketConfig, @Qualifier(STORAGE_CHANNEL) channel : MessageChannel, template : PubSubTemplate) =
        PubSubInboundChannelAdapter(template, cfg.mellom.subscription.navn).apply {
            setOutputChannel(channel)
            ackMode = MANUAL
        }

    @Bean
    @ServiceActivator(inputChannel = STORAGE_CHANNEL)
    fun messageReceiver( minside: MinSideClient, cfg: BucketConfig, mapper: ObjectMapper, metrikker: Metrikker) =
        StoragePubMessageHandler(minside,cfg,mapper,metrikker)


    class StoragePubMessageHandler(private val minside: MinSideClient, private val cfg: BucketConfig, private val mapper: ObjectMapper, private val  metrikker: Metrikker) :
        MessageHandler {
        override fun handleMessage(m : Message<*>) {
            m.headers.get(ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage::class.java)?.let {
                it.apply {
                    ack()
                    pubsubMessage.handle(minside,cfg,mapper,metrikker)
                }
            }
        }
    }


    companion object  {
        private const val STORAGE_CHANNEL = "storageInputChannel"
    }
}