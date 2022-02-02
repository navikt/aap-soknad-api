package no.nav.aap.api.oppslag.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.oppslag.system.SystemTokenTjeneste
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.Constants.NAV_CONSUMER_TOKEN
import no.nav.aap.util.Constants.PDL_USER
import no.nav.aap.util.Constants.PDL_SYSTEM
import no.nav.aap.util.Constants.TEMA
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
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import reactor.netty.http.client.HttpClient
import org.springframework.http.HttpHeaders.AUTHORIZATION


@Configuration
class PDLClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String) {

    @Qualifier(PDL_SYSTEM)
    @Bean
    fun pdlSystemWebClient(builder: Builder, cfg: PDLConfig, sts: SystemTokenTjeneste): WebClient? {
        return builder
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction(applicationName))
            .filter(pdlSystemUserExchangeFilterFunction(sts))
            .build()
    }

    private fun pdlSystemUserExchangeFilterFunction(sts: SystemTokenTjeneste) =
         ExchangeFilterFunction { req: ClientRequest, next: ExchangeFunction ->
            next.exchange(
                    ClientRequest.from(req)
                        .header(AUTHORIZATION, sts.bearerToken())
                        .header(TEMA, AAP)
                        .header(NAV_CONSUMER_TOKEN, sts.bearerToken())
                        .build())
    }

    @Qualifier(PDL_USER)
    @Bean
    fun pdlUserWebClient(builder: Builder, cfg: PDLConfig, tokenXFilterFunction: TokenXFilterFunction, env: Environment) =
        builder
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().wiretap(isDevOrLocal(env))))
            .baseUrl(cfg.baseUri.toString())
            .filter(correlatingFilterFunction(applicationName))
            .filter(temaFilterFunction())
            .filter(tokenXFilterFunction)
            .build()

    @Qualifier(PDL_USER)
    @Bean
    fun graphQlUserWebClient(@Qualifier(PDL_USER) client: WebClient, mapper: ObjectMapper): GraphQLWebClient =
        GraphQLWebClient.newInstance(client, mapper)

    @Bean
    fun pdlHealthIndicator(adapter: PDLWebClientAdapter) = object: AbstractPingableHealthIndicator(adapter){
    }
}