package no.nav.aap.api.oppslag.behandler

import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.FASTLEGE
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.rest.tokenx.TokenXFilterFunction
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
class BehandlerClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String) {

    @Qualifier(FASTLEGE)
    @Bean
    fun behandlereWebClient(builder: Builder, cfg: BehandlerConfig, filter: TokenXFilterFunction, env: Environment) =
        builder
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(isDevOrLocal(env))))
            .baseUrl(cfg.baseUri.toString())
            .filter(AbstractWebClientAdapter.correlatingFilterFunction(applicationName))
            .filter(filter)
            .build()

    @Bean
    fun behandlerHealthIndicator(a: BehandlerClientAdapter) = object: AbstractPingableHealthIndicator(a){
    }
}