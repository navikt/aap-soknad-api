package no.nav.aap.api.oppslag.krr

import no.nav.aap.api.oppslag.krr.KRRConfig.Companion.KRR
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter as felles
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
            //.filter(felles.correlatingFilterFunction(applicationName))
            .filter(felles.generellFilterFunction(Constants.NAV_PERSON_IDENT) { ctx.getSubject() ?: "unauthenticated" })
            .filter(tokenXFilterFunction)
            .build()

    @Bean
    fun krrHealthIndicator(a: KRRWebClientAdapter) = object: AbstractPingableHealthIndicator(a){
    }
}