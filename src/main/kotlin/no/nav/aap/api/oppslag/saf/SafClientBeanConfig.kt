package no.nav.aap.api.oppslag.saf

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.oppslag.saf.SafConfig.Companion.SAF
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class SafClientBeanConfig {

    @Qualifier(SAF)
    @Bean
    fun safWebClient(b: Builder, cfg: SafConfig, tokenXFilterFunction: TokenXFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(tokenXFilterFunction)
            .build()

    @Bean
    fun safHealthIndicator(a: SafWebClientAdapter) =
        object : AbstractPingableHealthIndicator(a) {}

    @Qualifier(SAF)
    @Bean
    fun graphQLSafWebClient(@Qualifier(SAF) client: WebClient, mapper: ObjectMapper) =
        GraphQLWebClient.newInstance(client, mapper)
}