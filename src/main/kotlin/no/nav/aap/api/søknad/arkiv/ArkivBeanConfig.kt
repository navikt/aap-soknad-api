package no.nav.aap.api.søknad.arkiv

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.*
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder
import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.ARKIVHENDELSER
import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.CLIENT_CREDENTIALS_ARKIV
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.health.Pingable
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.TokenExtensions.bearerToken
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties

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
            consumerFactory = DefaultKafkaConsumerFactory<String?, JournalfoeringHendelseRecord?>(p.buildConsumerProperties().apply {
               setRecordFilterStrategy { AAP != it.value().temaNytt.lowercase() }
            }).apply {
                containerProperties.isObservationEnabled = true
            }
        }


    @ConditionalOnProperty("$JOARK.enabled", havingValue = "true")
    @Qualifier(JOARK)
    @Bean
    fun arkivHendelserPingable(admin: KafkaAdmin, p: KafkaProperties, cfg: ArkivConfig)  = object : Pingable {
        override fun isEnabled() = cfg.isEnabled

        override fun name() = cfg.name

        override fun ping() =
            admin.describeTopics(cfg.hendelser.topic).entries
                .withIndex()
                .associate {
                    with(it) {
                        "topic-${index}" to "${value.value.name()} (${value.value.partitions().count()} partisjoner)"
                    }
                }

        override fun pingEndpoint() = "${p.bootstrapServers}"
    }

    @ConditionalOnProperty("$JOARK.enabled", havingValue = "true")
    @Bean
    fun arkivHendelserHealthIndicator(@Qualifier(JOARK) pingable: Pingable) = object : AbstractPingableHealthIndicator(pingable) {}

    @Bean
    @ConditionalOnProperty("$JOARK.enabled", havingValue = "true")
    fun arkivHealthIndicator(adapter: ArkivWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}