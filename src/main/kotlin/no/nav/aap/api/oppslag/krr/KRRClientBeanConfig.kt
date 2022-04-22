package no.nav.aap.api.oppslag.krr

import no.nav.aap.api.oppslag.krr.KRRConfig.Companion.KRR
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.generellFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.NAV_PERSON_IDENT
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class KRRClientBeanConfig {

    @Qualifier(KRR)
    @Bean
    fun krrWebClient(builder: Builder, cfg: KRRConfig, tokenXFilterFunction: TokenXFilterFunction, ctx: AuthContext) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(generellFilterFunction(NAV_PERSON_IDENT) { ctx.getSubject() ?: "Unauthenticated" })
            .filter(tokenXFilterFunction)
            .build()

    @Bean
    fun krrHealthIndicator(a: KRRWebClientAdapter) =
        object : AbstractPingableHealthIndicator(a) {}
}