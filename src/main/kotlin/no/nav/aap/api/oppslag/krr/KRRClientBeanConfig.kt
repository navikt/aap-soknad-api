package no.nav.aap.api.oppslag.krr

import no.nav.aap.api.oppslag.krr.KRRConfig.Companion.KRR
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter
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
class KRRClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String) {

    @Qualifier(KRR)
    @Bean
    fun krrWebClient(builder: Builder, cfg: KRRConfig, tokenXFilterFunction: TokenXFilterFunction, ctx: AuthContext, env: Environment) =
        builder
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(isDevOrLocal(env))))
            .baseUrl(cfg.baseUri.toString())
            .filter(AbstractWebClientAdapter.correlatingFilterFunction(applicationName))
            .filter(navPersonIdentFilterFunction(ctx))
            .filter(tokenXFilterFunction)
            .build()

    @Bean
    fun krrHealthIndicator(a: KRRWebClientAdapter) = object: AbstractPingableHealthIndicator(a){
    }

    fun navPersonIdentFilterFunction(ctx: AuthContext) =
        ExchangeFilterFunction { req: ClientRequest, next: ExchangeFunction ->
            next.exchange(
                    ClientRequest.from(req)
                        .header(Constants.NAV_PERSON_IDENT, ctx.getSubject())
                        .build())
        }
}