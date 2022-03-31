package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdConfig.Companion.ARBEIDSFORHOLD
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.rest.MDCPropagatingFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants
import no.nav.boot.conditionals.EnvUtil.isDevOrLocal
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient.Builder
import reactor.netty.http.client.HttpClient


@Configuration
class ArbeidsforholdClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String) {

    @Bean
    @Qualifier(ARBEIDSFORHOLD)
    fun webClientArbeidsforhold(builder: Builder, cfg: ArbeidsforholdConfig, tokenXFilter: TokenXFilterFunction, ctx: AuthContext, env: Environment) =
         builder
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(isDevOrLocal(env))))
             .baseUrl(cfg.baseUri.toString())
             .filter(navPersonIdentFunction(ctx))
             .filter(correlatingFilterFunction(applicationName))
             .filter(tokenXFilter)
            .build()


    private fun navPersonIdentFunction(ctx: AuthContext) =
         ExchangeFilterFunction { req: ClientRequest, next: ExchangeFunction ->
            next.exchange(ClientRequest.from(req)
                        .header(Constants.NAV_PERSON_IDENT, ctx.getSubject())
                        .build())
    }
    @Bean
    fun arbeidsforholdHealthIndicator(a: ArbeidsforholdClientAdapter) = object: AbstractPingableHealthIndicator(a){
    }
}