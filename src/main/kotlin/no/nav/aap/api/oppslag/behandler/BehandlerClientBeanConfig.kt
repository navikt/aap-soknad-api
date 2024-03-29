package no.nav.aap.api.oppslag.behandler

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder
import reactor.util.retry.Retry.*
import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLER
import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLERPING
import no.nav.aap.api.søknad.arkiv.ArkivConfig
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.LoggerUtil

@Configuration
class BehandlerClientBeanConfig {

    val log = LoggerUtil.getLogger(javaClass)
    @Qualifier(BEHANDLER)
    @Bean
    fun behandlereWebClient(builder: Builder, cfg: BehandlerConfig, tokenX: TokenXFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(tokenX)
            .build()

    @Qualifier(BEHANDLERPING)
    @Bean
    fun pingBehandlerWebClient(builder: Builder, cfg: ArkivConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    fun behandlerHealthIndicator(a: BehandlerWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}