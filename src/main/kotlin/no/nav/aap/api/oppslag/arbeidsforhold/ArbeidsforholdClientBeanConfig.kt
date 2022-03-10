package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdConfig.Companion.ARBEIDSFORHOLD
import no.nav.aap.api.oppslag.fastlege.FastlegeClientAdapter
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.boot.conditionals.EnvUtil
import no.nav.boot.conditionals.EnvUtil.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import reactor.netty.http.client.HttpClient


@Configuration
class ArbeidsforholdClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String) {

    @Bean
    @Qualifier(ARBEIDSFORHOLD)
    fun webClientArbeidsforhold(builder: Builder,
                                      cfg: ArbeidsforholdConfig,
                                      filter: TokenXFilterFunction,env: Environment): WebClient {
        return builder
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(isDevOrLocal(env))))
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction(applicationName))
            .filter(filter)
            .build()
    }

    @Bean
    fun arbeidsforholdHealthIndicator(a: ArbeidsforholdClientAdapter) = object: AbstractPingableHealthIndicator(a){
    }
}