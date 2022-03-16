package no.nav.aap.api.oppslag.organisasjon

import no.nav.aap.api.oppslag.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.boot.conditionals.EnvUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient

@Configuration
class OrganisasjonClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String, val cfg: OrganisasjonConfig) {

    @Bean
    @Qualifier(ORGANISASJON)
    fun organisasjonkWebClient(builder: WebClient.Builder,env: Environment) =
        builder
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(EnvUtil.isDevOrLocal(env))))
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction(applicationName))
            .build()

    @Bean
    fun organisasjonHealthIndicator(a: OrganisasjonWebClientAdapter) = object : AbstractPingableHealthIndicator(a){
    }
}