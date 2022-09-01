package no.nav.aap.api.søknad.arkiv

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.Constants.JOARK
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ArkivBeanConfig {
    @Qualifier(JOARK)
    @Bean
    fun webClientArkiv(builder: WebClient.Builder, cfg: ArkivConfig, tokenXFilterFunction: TokenXFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(temaFilterFunction())
            .filter(tokenXFilterFunction)
            .build()

    @Bean
    fun arkivHealthIndicator(adapter: ArkivWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}