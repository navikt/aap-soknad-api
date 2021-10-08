package no.nav.aap.api.søknad.rest;

import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;


public abstract class AbstractWebClientAdapter implements RetryAware, PingEndpointAware {
    protected final WebClient webClient;
    protected final AbstractRestConfig cfg;

    public AbstractWebClientAdapter(WebClient webClient, AbstractRestConfig cfg) {
        this.webClient = webClient;
        this.cfg = cfg;
    }

    @Override
    public String ping() {
        return webClient
                .get()
                .uri(pingEndpoint())
                .accept(APPLICATION_JSON, TEXT_PLAIN)
                .retrieve()
                .toEntity(String.class)
                .block()
                .getBody();
    }

    @Override
    public String name() {
        return cfg.name();
    }

    protected URI getBaseUri() {
        return cfg.getBaseUri();
    }

    @Override
    public URI pingEndpoint() {
        return cfg.pingEndpoint();
    }
}
