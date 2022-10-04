package no.nav.aap.api.oppslag.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAFQL
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class ArkivOppslagClientBeanConfig {

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