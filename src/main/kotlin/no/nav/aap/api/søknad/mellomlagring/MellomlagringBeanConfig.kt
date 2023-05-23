package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.ServiceOptions
import com.google.cloud.spring.pubsub.core.PubSubTemplate
import com.google.cloud.spring.pubsub.integration.AckMode.AUTO_ACK
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
import org.springframework.integration.annotation.Transformer
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.handler.annotation.Header
import org.threeten.bp.Duration
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.GCPEventType.ENDELIG_SLETTING
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.GCPEventType.IGNORER
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.GCPEventType.OPPDATERING
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.TestTransformer.GCPEventType.OPPRETTET
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.Metadata
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.endeligSlettet
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.eventType
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.førstegangsOpprettelse
import no.nav.aap.api.søknad.mellomlagring.PubSubMessageExtensions.metadata
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
    fun gcpStorageFlow(@Qualifier(STORAGE_CHANNEL) channel: MessageChannel, eventHandler: MellomlagringEventSubscriber/*, transformer: TestTransformer*/) =
        integrationFlow {
            channel(channel)
            wireTap {
                handle {
                    log.trace("Headers: {}", it.headers)
                }
            }
            transform(testTransformer())
            handle(eventHandler)
        }

   //@Bean
    fun testTransformer() = TestTransformer()

    class TestTransformer {

        private val log = LoggerUtil.getLogger(javaClass)

        @Transformer
        fun payload(@Header(ORIGINAL_MESSAGE) msg : BasicAcknowledgeablePubsubMessage?)  =
          try {
            msg?.pubsubMessage?.let {
                val md = it.metadata(jacksonObjectMapper())
                log.trace("Metadata er {}", md)
                when ( it.eventType()) {
                    OBJECT_FINALIZE -> if (it.førstegangsOpprettelse()) MellomlagringsHendelse(OPPRETTET,md) else MellomlagringsHendelse(OPPDATERING,md)
                    OBJECT_DELETE -> if (it.endeligSlettet()) MellomlagringsHendelse(ENDELIG_SLETTING,md)else MellomlagringsHendelse(IGNORER,md)
                    else -> MellomlagringsHendelse(IGNORER,md)
                }
            } ?: MellomlagringsHendelse(IGNORER)
          } catch (e: Exception) {
              log.warn("OOPS",e)
          }

        enum class GCPEventType {
            OPPRETTET, OPPDATERING,ENDELIG_SLETTING, IGNORER
        }
        data class MellomlagringsHendelse(val type : GCPEventType, val metadata : Metadata? = null)
    }
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