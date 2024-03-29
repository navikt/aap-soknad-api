package no.nav.aap.api.oppslag.arkiv

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import no.nav.aap.api.felles.graphql.LoggingGraphQLInterceptor
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAFQL
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.LoggerUtil.getLogger

@Configuration(proxyBeanMethods = false)
class ArkivOppslagClientBeanConfig {

    val log = getLogger(javaClass)

    @Qualifier(SAF)
    @Bean
    fun arkivOppslagWebClient(b: Builder, cfg: ArkivOppslagConfig, tokenX: TokenXFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(tokenX)
            .build()

    @Qualifier(SAFQL)
    @Bean
    fun arkivOppslagGraphQLWebClient(b: Builder, cfg: ArkivOppslagConfig, tokenX: TokenXFilterFunction) =
        b.baseUrl("${cfg.baseUri}/graphql")
            .filter(tokenX)
            .build()

    @Bean
    @ConditionalOnProperty("$SAF.enabled", havingValue = "true")
    fun arkivOppslagHealthIndicator(a: ArkivOppslagWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

    @Qualifier(SAF)
    @Bean
    fun arkivOppslagGraphQLClient(@Qualifier(SAFQL) client: WebClient) = HttpGraphQlClient.builder(client)
        .interceptor(LoggingGraphQLInterceptor())
        .build()
}