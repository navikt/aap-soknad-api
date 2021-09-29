package no.nav.aap.helloworld;

import no.nav.foreldrepenger.boot.conditionals.ConditionalOnK8s;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Optional;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

//@ConditionalOnK8s
@Order(HIGHEST_PRECEDENCE)
public class TokenXClientRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(TokenXClientRequestInterceptor.class);
    private final ClientConfigurationProperties configs;
    private final OAuth2AccessTokenService service;
    private final TokenXConfigFinder finder;

    public TokenXClientRequestInterceptor(ClientConfigurationProperties configs,
                                                 OAuth2AccessTokenService service, TokenXConfigFinder finder) {
        this.configs = configs;
        this.service = service;
        this.finder = finder;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest req, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        LOG.trace("Intercepting {}", req.getURI());
        Optional.ofNullable(finder.findProperties(configs, req.getURI()))
                .ifPresentOrElse(config -> req.getHeaders().setBearerAuth(service.getAccessToken(config).getAccessToken()),
                        () -> LOG.info("Ingen konfig for {}", req.getURI()));
        return execution.execute(req, body);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [configs=" + configs + ", service=" + service + ", finder=" + finder + "]";
    }
}
