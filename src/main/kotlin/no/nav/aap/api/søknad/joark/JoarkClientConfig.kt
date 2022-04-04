package no.nav.aap.api.søknad.joark

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.aap.util.Constants.JOARK
import no.nav.boot.conditionals.EnvUtil.isDevOrLocal
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class JoarkClientConfig(@Value("\${spring.application.name}") val applicationName: String) {

    @Qualifier(JOARK)
    @Bean
    fun webClientJoark(builder: WebClient.Builder,
                       cfg: JoarkConfig,
                       aadFilterFunction: AADFilterFunction,
                       env: Environment) =
        builder
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(isDevOrLocal(env))))
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction(applicationName))
            .filter(temaFilterFunction())
            .filter(aadFilterFunction)
            .build()

    @Bean
    fun joarkHealthIndicator(adapter: JoarkClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {
    }
}