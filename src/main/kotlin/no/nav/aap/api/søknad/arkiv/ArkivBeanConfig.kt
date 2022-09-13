package no.nav.aap.api.søknad.arkiv

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.temaFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.Constants.JOARK
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ArkivBeanConfig {
    @Qualifier(JOARK)
    @Bean
    fun webClientArkiv(builder: WebClient.Builder, cfg: ArkivConfig, @Qualifier(JOARK) arkivClientCredentialFilterFunction:) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(temaFilterFunction())
            .filter(arkivClientCredentialFilterFunction)
            .build()

    @Bean
    @Qualifier(JOARK)
    fun arkivClientCredentialFilterFunction(cfgs: ClientConfigurationProperties, service: OAuth2AccessTokenService) =
        ExchangeFilterFunction { req, next ->
            next.exchange(ClientRequest.from(req).header(AUTHORIZATION, service.systemBearerToken(cfgs)).build())
        }

    private fun OAuth2AccessTokenService.systemBearerToken(cfgs: ClientConfigurationProperties) =
        getAccessToken(cfgs.registration[CLIENT_CREDENTIALS_ARKIV]).accessToken.asBearer()

    @Bean
    fun arkivHealthIndicator(adapter: ArkivWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}