package no.nav.aap.api.oppslag.konto

import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.AuthContext
import no.nav.aap.util.MDCUtil.callId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class KontoClientBeanConfig {

    @Qualifier(KONTO)
    @Bean
    fun kontoWebClient(b: Builder, cfg: KontoConfig, tokenXFilterFunction: TokenXFilterFunction, ctx: AuthContext) =
        b.baseUrl("${cfg.baseUri}")
            .filter(AbstractWebClientAdapter.generellFilterFunction("nav-call-id") {
                callId()
            })
            .filter(tokenXFilterFunction)
            .build()

    @Bean
    fun kontoHealthIndicator(a: KontoWebClientAdapter) =
        object : AbstractPingableHealthIndicator(a) {}
}