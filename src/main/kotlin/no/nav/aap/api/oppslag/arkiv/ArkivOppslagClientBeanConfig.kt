package no.nav.aap.api.oppslag.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import java.time.Duration
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAFQL
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.LoggerUtil
import org.hibernate.secure.spi.IntegrationException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import reactor.util.retry.Retry

@Configuration
class ArkivOppslagClientBeanConfig {

    val log = LoggerUtil.getLogger(javaClass)

    @Bean
    @Qualifier(SAF)
    fun retry(): Retry =
        Retry.fixedDelay(3, Duration.ofMillis(100))
            .filter { e -> e is IntegrationException }
            .doBeforeRetry { s -> log.warn("Retry kall mot token endpoint grunnet exception ${s.failure().javaClass.name} og melding ${s.failure().message} for ${s.totalRetriesInARow() + 1} gang, prøver igjen") }
            .onRetryExhaustedThrow { _, spec ->  throw IntegrationException("Retry kall mot token endpoint gir opp etter ${spec.totalRetries()} forsøk",spec.failure()) }


    @Qualifier(SAF)
    @Bean
    fun arkivOppslagWebClient(b: Builder, cfg: ArkivOppslagConfig, tokenX: TokenXFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(tokenX)
            .build()

    @Qualifier(SAFQL)
    @Bean
    fun arkivOppslagQLWebClient(b: Builder, cfg: ArkivOppslagConfig, tokenX: TokenXFilterFunction) =
        b.baseUrl("${cfg.baseUri}/graphql")
            .filter(tokenX)
            .build()

    @Bean
    @ConditionalOnProperty("$SAF.enabled", havingValue = "true")
    fun arkivOppslagHealthIndicator(a: ArkivOppslagWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

    @Qualifier(SAF)
    @Bean
    fun arkivOppslagGraphQLWebClient(@Qualifier(SAFQL) client: WebClient, mapper: ObjectMapper) = GraphQLWebClient.newInstance(client, mapper)
}