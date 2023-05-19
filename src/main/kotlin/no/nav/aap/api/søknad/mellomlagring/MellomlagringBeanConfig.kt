package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.ServiceOptions
import com.google.cloud.spring.pubsub.core.PubSubTemplate
import com.google.cloud.spring.pubsub.integration.AckMode.MANUAL
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders.*
import com.google.cloud.storage.NotificationInfo.*
import com.google.cloud.storage.NotificationInfo.EventType.*
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.threeten.bp.Duration
import no.nav.aap.api.config.Metrikker
import no.nav.aap.api.søknad.minside.MinSideClient

@Configuration(proxyBeanMethods = false)
class MellomlagringBeanConfig {

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
    fun storageChannelAdapter(cfg: BucketConfig, template : PubSubTemplate) =
        PubSubInboundChannelAdapter(template, cfg.mellom.subscription.navn).apply {
            outputChannel = DirectChannel()
            ackMode = MANUAL
        }

    @Bean
    @ServiceActivator(inputChannel = STORAGE_CHANNEL)
    fun messageReceiver( minside: MinSideClient, cfg: BucketConfig, mapper: ObjectMapper, metrikker: Metrikker) =
        StoragePubMessageHandler(minside,cfg,mapper,metrikker)

    companion object  {
        private const val STORAGE_CHANNEL = "storageInputChannel"
    }
}