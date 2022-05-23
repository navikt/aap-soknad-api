package no.nav.aap.api.oppslag.saf

import no.nav.aap.api.oppslag.saf.SafConfig.Companion.SAF
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class SafClientBeanConfig {

    @Qualifier(SAF)
    @Bean
    fun krrWebClient(b: Builder, cfg: SafConfig, tokenXFilterFunction: TokenXFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(tokenXFilterFunction)
            .build()

    @Bean
    fun krrHealthIndicator(a: SafWebClientAdapter) =
        object : AbstractPingableHealthIndicator(a) {}
}