package no.nav.aap.api.søknad.rest;

import java.net.URI;

public interface PingEndpointAware extends Pingable {
    URI pingEndpoint();
    String name();
}
