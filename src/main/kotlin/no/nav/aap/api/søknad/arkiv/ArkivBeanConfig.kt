package no.nav.aap.api.søknad.arkiv

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.*
import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.ARKIVHENDELSER
import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.CLIENT_CREDENTIALS_ARKIV
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.TokenExtensions.bearerToken
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
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

    @Bean(ARKIVHENDELSER)
    fun arkivHendelserListenerContainerFactory(p: KafkaProperties) =
        ConcurrentKafkaListenerContainerFactory<String, JournalfoeringHendelseRecord>().apply {
            consumerFactory = DefaultKafkaConsumerFactory(p.buildConsumerProperties().apply {
                put(SPECIFIC_AVRO_READER_CONFIG, true)
                setRecordFilterStrategy { !AAP.equals(it.value().temaNytt, true) }
            })
        }

    @Bean
    @ConditionalOnProperty("$JOARK.enabled", havingValue = "true")
    fun arkivHealthIndicator(adapter: ArkivWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}