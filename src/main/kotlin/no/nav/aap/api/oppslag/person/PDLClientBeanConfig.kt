package no.nav.aap.api.oppslag.person

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import no.nav.aap.api.oppslag.graphql.GraphQLInterceptor
import no.nav.aap.api.oppslag.person.PDLConfig.Companion.PDL_CREDENTIALS
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.behandlingFilterFunction
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.Constants.PDL_SYSTEM
import no.nav.aap.util.Constants.PDL_USER
import no.nav.aap.util.TokenExtensions.bearerToken
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties

@Configuration(proxyBeanMethods = false)
class PDLClientBeanConfig {

    @Bean
    @Qualifier(PDL_SYSTEM)
    fun pdlSystemWebClient(b: Builder, cfg: PDLConfig, @Qualifier(PDL_SYSTEM) pdlClientCredentialFilterFunction: ExchangeFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(temaFilterFunction())
            .filter(behandlingFilterFunction())
            .filter(pdlClientCredentialFilterFunction)
            .build()

    @Bean
    @Qualifier(PDL_SYSTEM)
    fun pdlClientCredentialFilterFunction(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            next.exchange(ClientRequest.from(req).header(AUTHORIZATION, service.bearerToken(cfgs.registration[PDL_CREDENTIALS], req.url())).build())
        }

    @Bean
    @Qualifier(PDL_USER)
    fun graphQLBootClient(@Qualifier(PDL_USER) client : WebClient, mapper : ObjectMapper) =
        HttpGraphQlClient.builder(client)
            .interceptor(GraphQLInterceptor())
            .build()

    @Qualifier(PDL_SYSTEM)
    @Bean
    fun graphQLSystemWebClient(@Qualifier(PDL_SYSTEM) client: WebClient, mapper: ObjectMapper) = GraphQLWebClient.newInstance(client, mapper)

    @Qualifier(PDL_SYSTEM)
    @Bean
    fun graphQLBootSystemWebClient(@Qualifier(PDL_SYSTEM) client: WebClient) =   HttpGraphQlClient.builder(client)
        .interceptor(GraphQLInterceptor())
        .build()
    @Qualifier(PDL_USER)
    @Bean
    fun pdlUserWebClient(b: Builder, cfg: PDLConfig, tokenX: TokenXFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(temaFilterFunction())
            .filter(behandlingFilterFunction())
            .filter(tokenX)
            .build()

    @Qualifier(PDL_USER)
    @Bean
    fun graphQLUserWebClient(@Qualifier(PDL_USER) client: WebClient, mapper: ObjectMapper) = GraphQLWebClient.newInstance(client, mapper)

    @Bean
    @ConditionalOnProperty("${PDL_USER}.enabled", havingValue = "true")
    fun pdlHealthIndicator(a: PDLWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}