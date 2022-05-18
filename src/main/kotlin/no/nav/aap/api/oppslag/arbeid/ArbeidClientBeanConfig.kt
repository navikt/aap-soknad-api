package no.nav.aap.api.oppslag.arbeid

import no.nav.aap.api.oppslag.arbeid.ArbeidConfig.Companion.ARBEID
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.generellFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.AuthContext
import no.nav.aap.util.MDCUtil.NAV_PERSON_IDENT
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class ArbeidClientBeanConfig {

    @Bean
    @Qualifier(ARBEID)
    fun webClientArbeidsforhold(builder: Builder,
                                cfg: ArbeidConfig,
                                tokenXFilter: TokenXFilterFunction,
                                ctx: AuthContext) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(navPersonIdentFunction(ctx))
            .filter(tokenXFilter)
            .build()

    private fun navPersonIdentFunction(ctx: AuthContext) = generellFilterFunction(NAV_PERSON_IDENT) {
        ctx.getSubject() ?: "NO SUBJECT" //throw JwtTokenMissingException()
    }

    @Bean
    fun arbeidsforholdHealthIndicator(a: ArbeidWebClientAdapter) =
        object : AbstractPingableHealthIndicator(a) {}

}