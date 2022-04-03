package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdConfig.Companion.ARBEIDSFORHOLD
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.generellFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.NAV_PERSON_IDENT
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder


@Configuration
class ArbeidsforholdClientBeanConfig

    @Bean
    @Qualifier(ARBEIDSFORHOLD)
    fun webClientArbeidsforhold(builder: Builder, cfg: ArbeidsforholdConfig, tokenXFilter: TokenXFilterFunction, ctx: AuthContext) =
         builder
             .baseUrl("${cfg.baseUri}")
             .filter(navPersonIdentFunction(ctx))
             .filter(tokenXFilter)
            .build()


    private fun navPersonIdentFunction(ctx: AuthContext) = generellFilterFunction(NAV_PERSON_IDENT) {
        ctx.getSubject() ?: throw JwtTokenMissingException()
    }

    @Bean
    fun arbeidsforholdHealthIndicator(a: ArbeidsforholdClientAdapter) = object: AbstractPingableHealthIndicator(a){}