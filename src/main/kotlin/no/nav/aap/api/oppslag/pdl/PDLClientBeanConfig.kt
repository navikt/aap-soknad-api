package no.nav.aap.api.oppslag.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.Constants.PDL_SYSTEM
import no.nav.aap.util.Constants.PDL_USER
import no.nav.aap.util.StringExtensions.asBearer
import no.nav.boot.conditionals.EnvUtil.isDevOrLocal
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.*
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import reactor.netty.http.client.HttpClient


@Configuration
class PDLClientBeanConfig(@Value("\${spring.application.name}") val applicationName: String) {


    @Bean
    @Qualifier(PDL_SYSTEM)
    fun webClientPDLSystem(@Value("\${pdl.base-uri}") baseUri: String, builder: Builder,  @Qualifier(PDL_SYSTEM) aadPDLFilterFunction: ExchangeFilterFunction)  =
        builder
            .baseUrl(baseUri)
            .filter(correlatingFilterFunction(applicationName))
            .filter(temaFilterFunction())
            .filter(aadPDLFilterFunction)
            .build()

    @Bean
    @Qualifier(PDL_SYSTEM)
    fun aadPDLClientCredentialFilterFunction(configs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            val token = service.getAccessToken(configs.registration["client-credentials-pdl"]).accessToken
            next.exchange(ClientRequest.from(req).header(AUTHORIZATION, token.asBearer()).build())
        }

    @Qualifier(PDL_SYSTEM)
    @Bean
    fun graphQlSystemWebClient(@Qualifier(PDL_SYSTEM) client: WebClient, mapper: ObjectMapper): GraphQLWebClient =
        GraphQLWebClient.newInstance(client, mapper)

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
