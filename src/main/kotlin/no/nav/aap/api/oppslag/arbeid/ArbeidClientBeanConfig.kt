package no.nav.aap.api.oppslag.arbeid

import no.nav.aap.api.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.generellFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.AuthContext
import no.nav.aap.util.MDCUtil.NAV_PERSON_IDENT
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
@ConditionalOnProperty("$ARBEID.enabled", havingValue = "true")
class ArbeidClientBeanConfig {

    @Bean
    @Qualifier(ARBEID)
    fun webClientArbeidsforhold(builder: Builder, cfg: ArbeidConfig, tokenX: TokenXFilterFunction, ctx: AuthContext) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(navPersonIdentFunction(ctx))
            .filter(tokenX)
            .build()

    private fun navPersonIdentFunction(ctx: AuthContext) = generellFilterFunction(NAV_PERSON_IDENT) {
        ctx.getSubject() ?: "NO SUBJECT"
    }

    @Bean
    fun arbeidsforholdHealthIndicator(a: ArbeidWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}

}