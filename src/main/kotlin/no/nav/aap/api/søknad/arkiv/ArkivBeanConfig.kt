package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.søknad.arkiv.ArkivConfig.Companion.CLIENT_CREDENTIALS_ARKIV
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.Constants.JOARK
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.asBearer
import no.nav.boot.conditionals.EnvUtil
import no.nav.boot.conditionals.EnvUtil.*
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ArkivBeanConfig {

    private val log = getLogger(javaClass)

    @Qualifier(JOARK)
    @Bean
    fun webClientArkiv(builder: WebClient.Builder, cfg: ArkivConfig, @Qualifier(JOARK) arkivClientCredentialFilterFunction: ExchangeFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(arkivClientCredentialFilterFunction)
            .build()

    @Bean
    @Qualifier(JOARK)
    fun arkivClientCredentialFilterFunction(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            log.trace(CONFIDENTIAL, "Gjør token exchange for ${req.url()}")
            next.exchange(ClientRequest.from(req).header(AUTHORIZATION, service.systemBearerToken(cfgs)).build())
        }

    private fun OAuth2AccessTokenService.systemBearerToken(cfgs: ClientConfigurationProperties) =
        getAccessToken(cfgs.registration[CLIENT_CREDENTIALS_ARKIV]).accessToken.asBearer().also {
            log.trace(CONFIDENTIAL,"Token exchange for $CLIENT_CREDENTIALS_ARKIV  OK, token er $it")
        }

    @Bean
    fun arkivHealthIndicator(adapter: ArkivWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}