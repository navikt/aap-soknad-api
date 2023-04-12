package no.nav.aap.api.oppslag.arbeid

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder
import no.nav.aap.api.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.generellFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.AuthContext
import no.nav.aap.util.MDCUtil.NAV_PERSON_IDENT

@Configuration
class ArbeidClientBeanConfig {

    @Bean
    @Qualifier(ARBEID)
    fun webClientArbeidsforhold(builder: Builder, cfg: ArbeidConfig, tokenX: TokenXFilterFunction, ctx: AuthContext) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(generellFilterFunction(NAV_PERSON_IDENT) { ctx.getSubject() ?: "NO SUBJECT" })
            .filter(tokenX)
            .build()

    @Bean
    @ConditionalOnProperty("$ARBEID.enabled", havingValue = "true")
    fun arbeidsforholdHealthIndicator(a: ArbeidWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

}