package no.nav.aap.api.oppslag.kontaktinformasjon

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder
import no.nav.aap.api.oppslag.kontaktinformasjon.KRRConfig.Companion.KRR
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.generellFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.AuthContext
import no.nav.aap.util.MDCUtil.NAV_PERSON_IDENT

@Configuration
class KRRClientBeanConfig {

    @Qualifier(KRR)
    @Bean
    fun krrWebClient(b: Builder, cfg: KRRConfig, tokenX: TokenXFilterFunction, ctx: AuthContext) =
        b.baseUrl("${cfg.baseUri}")
            .filter(generellFilterFunction(NAV_PERSON_IDENT) { ctx.getSubject() ?: "Unauthenticated" })
            .filter(tokenX)
            .build()

    @Bean
    @ConditionalOnProperty("$KRR.enabled", havingValue = "true")
    fun krrHealthIndicator(a: KRRWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}