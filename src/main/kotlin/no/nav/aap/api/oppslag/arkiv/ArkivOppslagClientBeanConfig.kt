package no.nav.aap.api.oppslag.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagConfig.Companion.SAF
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class ArkivOppslagClientBeanConfig {

    @Qualifier(SAF)
    @Bean
    fun arkivOppslagWebClient(b: Builder, cfg: ArkivOppslagConfig, tokenXFilterFunction: TokenXFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(tokenXFilterFunction)
            .build()

    @Bean
    fun arkivOppslagHealthIndicator(a: ArkivOppslagWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

    @Qualifier(SAF)
    @Bean
    fun arkivOppslagGraphQLWebClient(@Qualifier(SAF) client: WebClient, mapper: ObjectMapper) =
        GraphQLWebClient.newInstance(client, mapper)
}