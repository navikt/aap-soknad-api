package no.nav.aap.api.rest.tokenx

import no.nav.aap.util.AuthContext
import no.nav.aap.util.AuthContext.Companion.bearerToken
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.LoggerUtil.getSecureLogger
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

@Component
class TokenXFilterFunction(
        private val configs: ClientConfigurationProperties,
        private val service: OAuth2AccessTokenService,
        private val matcher: TokenXConfigMatcher,
        private val authContext: AuthContext
                          ) : ExchangeFilterFunction {
    private val log = getLogger(javaClass)
    private val secureLog = getSecureLogger()

    override fun filter(req: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val url = req.url()
        log.trace("Sjekker token exchange for {}", url)
        val cfg = matcher.findProperties(configs, url)
        if (cfg != null && authContext.isAuthenticated()) {
            log.trace(CONFIDENTIAL, "Gjør token exchange for {} med konfig {}", url, cfg)
            val token = service.getAccessToken(cfg).accessToken
            log.trace("Token exchange for {} OK", url)
            secureLog.trace("Token er {}", token)
            return next.exchange(
                    ClientRequest.from(req).header(AUTHORIZATION, bearerToken(token)).build())
        }
        log.trace("Ingen token exchange for {}", url)
        return next.exchange(ClientRequest.from(req).build())
    }

    override fun toString() =
        "${javaClass.simpleName} [[configs=$configs,authContext=$authContext,service=$service,matcher=$matcher]"
}