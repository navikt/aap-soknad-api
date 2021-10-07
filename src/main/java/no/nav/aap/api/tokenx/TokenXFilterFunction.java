package no.nav.aap.api.tokenx;

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static no.nav.aap.api.util.TokenUtil.BEARER;
import static no.nav.foreldrepenger.boot.conditionals.EnvUtil.CONFIDENTIAL;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class TokenXFilterFunction implements ExchangeFilterFunction {

    private static final Logger LOG = LoggerFactory.getLogger(TokenXFilterFunction.class);

    private final OAuth2AccessTokenService service;
    private final TokenXConfigMatcher matcher;
    private final ClientConfigurationProperties configs;

    TokenXFilterFunction(ClientConfigurationProperties configs, OAuth2AccessTokenService service, TokenXConfigMatcher matcher) {
        this.service = service;
        this.matcher = matcher;
        this.configs = configs;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest req, ExchangeFunction next) {
        var url = req.url();
        LOG.trace("Sjekker token exchange for {}", url);
        var cfg = matcher.findProperties(configs, url);
        if (cfg.isPresent()) {
            LOG.trace(CONFIDENTIAL,"Gjør token exchange for {} med konfig {}", url, cfg);
            var token = service.getAccessToken(cfg.get()).getAccessToken();
            LOG.trace("Token exchange for {} OK", url);
            return next.exchange(ClientRequest.from(req).header(AUTHORIZATION, BEARER + token)
                    .build());
        }
        LOG.trace("Ingen token exchange for {}", url);
        return next.exchange(ClientRequest.from(req).build());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [service=" + service + ", matcher=" + matcher + ", configs=" + configs + "]";
    }
}
