package no.nav.aap.api.søknad.arkiv

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.*
import java.net.URI
import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.CLIENT_CREDENTIALS_ARKIV
import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.JOARKHENDELSER
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.asBearer
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class ArkivBeanConfig {

    private val log = getLogger(javaClass)

    @Qualifier(JOARK)
    @Bean
    fun webClientArkiv(builder: Builder, cfg: ArkivConfig, @Qualifier(JOARK) clientCredentialFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(clientCredentialFilterFunction)
            .build()

    @Qualifier("${JOARK}ping")
    @Bean
    fun pingWebClientArkiv(builder: Builder, cfg: ArkivConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    @Qualifier(JOARK)
    fun clientCredentialFilterFunction(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            next.exchange(ClientRequest.from(req)
                .header(AUTHORIZATION, service.bearerToken(cfgs.registration[CLIENT_CREDENTIALS_ARKIV], req.url()))
                .build())
        }

    private fun OAuth2AccessTokenService.bearerToken(properties: ClientProperties?, url: URI) =
        properties?.let { p ->
            log.trace(CONFIDENTIAL, "Gjør token exchange for $url med konfigurasjon fra $p")
            getAccessToken(p).accessToken.asBearer().also {
                log.trace("Token exchange for $url OK")
                log.trace(CONFIDENTIAL, "Token er $it")
            }
        } ?: throw IllegalArgumentException("Ingen konfigurasjon for $url")

    @Bean(JOARKHENDELSER)
    fun joarkHendelserListenerContainerFactory(p: KafkaProperties) =
        ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
            consumerFactory = DefaultKafkaConsumerFactory(p.buildConsumerProperties().apply {
                put(KEY_DESERIALIZER_CLASS, StringDeserializer::class.java)
                put(VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer::class.java)
                put(SPECIFIC_AVRO_READER_CONFIG, true)
                setRecordFilterStrategy { !AAP.equals(it.value().temaNytt, true) }
            })
        }

    @Bean
    @ConditionalOnProperty("$JOARK.enabled", havingValue = "true")
    fun arkivHealthIndicator(adapter: ArkivWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}