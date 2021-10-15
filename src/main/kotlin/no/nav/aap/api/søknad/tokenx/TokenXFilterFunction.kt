package no.nav.aap.api.søknad.tokenx

import no.nav.aap.api.søknad.tokenx.AuthContext.Companion.bearerToken
import no.nav.foreldrepenger.boot.conditionals.EnvUtil
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

@Component
class TokenXFilterFunction internal constructor(
    private val configs: ClientConfigurationProperties,
    private val service: OAuth2AccessTokenService,
    private val matcher: TokenXConfigMatcher,
    private val authContext: AuthContext
) : ExchangeFilterFunction {
    override fun filter(req: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        val url = req.url()
        LOG.trace("Sjekker token exchange for {}", url)
        val cfg = matcher.findProperties(
            configs, url
        )
        if (cfg != null && authContext.erAutentisert()) {
            LOG.trace(EnvUtil.CONFIDENTIAL, "Gjør token exchange for {} med konfig {}", url, cfg)
            val token = service.getAccessToken(cfg).accessToken
            LOG.trace("Token exchange for {} OK", url)
            return next.exchange(
                ClientRequest.from(req).header(HttpHeaders.AUTHORIZATION, bearerToken(token))
                    .build()
            )
        }
        LOG.trace("Ingen token exchange for {}", url)
        return next.exchange(ClientRequest.from(req).build())
    }

    override fun toString(): String {
        return javaClass.simpleName + " [authenticationContext=" + authContext + "service=" + service + ", matcher=" + matcher + ", configs=" + configs + "]"
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TokenXFilterFunction::class.java)
    }
}