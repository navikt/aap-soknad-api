package no.nav.aap.api.oppslag.konto

import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO_CREDENTIALS
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.StringExtensions.asBearer
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class KontoClientBeanConfig {

    @Qualifier(KONTO)
    @Bean
    fun kontoWebClient(
            b: Builder, cfg: KontoConfig,
            @Qualifier(KONTO) kontoClientCredentialFilterFunction: ExchangeFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(kontoClientCredentialFilterFunction)
            .build()

    @Bean
    @Qualifier(KONTO)
    fun kontoClientCredentialFilterFunction(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            next.exchange(ClientRequest.from(req).header(AUTHORIZATION, service.kontoSystemBearerToken(cfgs))
                .build())
        }

    private fun OAuth2AccessTokenService.kontoSystemBearerToken(cfgs: ClientConfigurationProperties) =
        getAccessToken(cfgs.registration[KONTO_CREDENTIALS]).accessToken.asBearer()

    @Bean
    fun kontoHealthIndicator(a: KontoWebClientAdapter) =
        object : AbstractPingableHealthIndicator(a) {}
}