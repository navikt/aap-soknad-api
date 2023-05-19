package no.nav.aap.api.søknad.mellomlagring

import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.ServiceOptions
import com.google.cloud.spring.pubsub.core.PubSubTemplate
import com.google.cloud.spring.pubsub.integration.AckMode.MANUAL
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders.*
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler
import org.threeten.bp.Duration
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
    @Qualifier("pubsubInputChannel")
    fun pubsubInputChannel() = DirectChannel()
    @Bean
    fun messageChannelAdapter(cfg: BucketConfig, @Qualifier("pubsubInputChannel") inputChannel : MessageChannel, pubSubTemplate : PubSubTemplate) =
        PubSubInboundChannelAdapter(pubSubTemplate, cfg.mellom.subscription.navn).apply {
            setOutputChannel(inputChannel)
            ackMode = MANUAL
        }

    @Bean
    @ServiceActivator(inputChannel = "pubsubInputChannel")
    fun messageReceiver() = MessageHandler { m ->
        log.info("Message arrived! Payload: " + String((m.payload as ByteArray)))
        val originalMessage  = m.headers.get(ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage::class.java)
        originalMessage?.ack()
    }
}