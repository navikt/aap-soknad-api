package no.nav.aap.api.oppslag.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.oppslag.pdl.PDLConfig.Companion.PDL_CREDENTIALS
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.Constants.PDL_SYSTEM
import no.nav.aap.util.Constants.PDL_USER
import no.nav.aap.util.StringExtensions.asBearer
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder


@Configuration
class PDLClientBeanConfig {

    @Bean
    @Qualifier(PDL_SYSTEM)
    fun pdlSystemWebClient(b: Builder, cfg: PDLConfig, @Qualifier(PDL_SYSTEM) aadPDLFilterFunction: ExchangeFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(temaFilterFunction())
            .filter(aadPDLFilterFunction)
            .build()

    @Bean
    @Qualifier(PDL_SYSTEM)
    fun aadPdlClientCredentialFilterFunction(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            next.exchange(ClientRequest.from(req).header(AUTHORIZATION, service.systemBearerToken(cfgs)).build())
        }

    private fun OAuth2AccessTokenService.systemBearerToken(cfgs: ClientConfigurationProperties) =
        getAccessToken(cfgs.registration[PDL_CREDENTIALS]).accessToken.asBearer()

    @Qualifier(PDL_SYSTEM)
    @Bean
    fun graphQlSystemWebClient(@Qualifier(PDL_SYSTEM) client: WebClient, mapper: ObjectMapper)  =
        GraphQLWebClient.newInstance(client, mapper)

    @Qualifier(PDL_USER)
    @Bean
    fun pdlUserWebClient(b: Builder, cfg: PDLConfig, tokenXFilterFunction: TokenXFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(temaFilterFunction())
            .filter(tokenXFilterFunction)
            .build()

    @Qualifier(PDL_USER)
    @Bean
    fun graphQlUserWebClient(@Qualifier(PDL_USER) client: WebClient, mapper: ObjectMapper) =
        GraphQLWebClient.newInstance(client, mapper)

    @Bean
    fun pdlHealthIndicator(a: PDLWebClientAdapter) =
        object : AbstractPingableHealthIndicator(a) {}
}