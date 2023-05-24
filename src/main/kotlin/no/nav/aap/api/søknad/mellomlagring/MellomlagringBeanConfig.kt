package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.ServiceOptions
import com.google.cloud.spring.pubsub.core.PubSubTemplate
import com.google.cloud.spring.pubsub.integration.AckMode.AUTO_ACK
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders.*
import com.google.cloud.storage.NotificationInfo.*
import com.google.cloud.storage.NotificationInfo.EventType.*
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.MessageChannel
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
    @Qualifier(STORAGE_CHANNEL)
    @Primary
    fun gcpStorageInputChannel() = DirectChannel()

    @Bean
    fun gcpStorageFlow(@Qualifier(STORAGE_CHANNEL) channel: MessageChannel, eventHandler: MellomlagringEventSubscriber, transformer: GCPBucketEventTransformer) =
        integrationFlow {
            channel(channel)
            wireTap {
                handle {
                    log.trace("Headers: {}", it.headers)
                }
            }
         //   transform(transformer)
            handle(eventHandler)
        }

   @Bean
    fun gcpEventTransformer(mapper: ObjectMapper) = GCPBucketEventTransformer(mapper)


    @Bean
    fun gcpStorageChannelAdapter(cfg: BucketConfig, template : PubSubTemplate,  @Qualifier(STORAGE_CHANNEL) channel: MessageChannel) =
        PubSubInboundChannelAdapter(template, cfg.mellom.subscription.navn).apply {
            outputChannel = channel
            ackMode = AUTO_ACK
        }

    companion object  {
         const val STORAGE_CHANNEL = "gcpStorageInputChannel"
    }
}