package no.nav.aap.api.oppslag.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.Constants.PDL_USER
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
class PDLClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String) {

    @Qualifier(PDL_USER)
    @Bean
    fun pdlWebClient(
            builder: WebClient.Builder,
            cfg: PDLConfig,
            tokenXFilterFunction: TokenXFilterFunction,
            env: Environment) =
        builder
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(isDevOrLocal(env))))
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction(applicationName))
            .filter(temaFilterFunction())
            .filter(tokenXFilterFunction)
            .build()

    @Qualifier(PDL_USER)
    @Bean
    fun graphQlWebClient(@Qualifier(PDL_USER) client: WebClient, mapper: ObjectMapper): GraphQLWebClient =
        GraphQLWebClient.newInstance(client, mapper)

    @Bean
    fun pdlHealthIndicator(adapter: PDLWebClientAdapter) = object: AbstractPingableHealthIndicator(adapter){
    }
}