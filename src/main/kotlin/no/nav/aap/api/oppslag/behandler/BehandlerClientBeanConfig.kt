package no.nav.aap.api.oppslag.behandler

import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLER
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class BehandlerClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String) {

    @Qualifier(BEHANDLER)
    @Bean
    fun behandlereWebClient(builder: Builder, cfg: BehandlerConfig, filter: TokenXFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(filter)
            .build()

    @Bean
    fun behandlerHealthIndicator(a: BehandlerWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}