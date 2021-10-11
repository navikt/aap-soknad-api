package no.nav.aap.api.søknad.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;


public abstract class AbstractWebClientAdapter implements RetryAware, Pingable {
    protected final WebClient webClient;
    protected final AbstractRestConfig cfg;

    public AbstractWebClientAdapter(WebClient webClient, AbstractRestConfig cfg) {
        this.webClient = webClient;
        this.cfg = cfg;
    }

    @Override
    public void ping() {
         webClient
                .get()
                .uri(pingEndpoint())
                .accept(APPLICATION_JSON, TEXT_PLAIN)
                .retrieve()
                 .onStatus(HttpStatus::isError, ClientResponse::createException)
                 .toBodilessEntity()
                .block();
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
