package no.nav.aap.api.søknad.rest;

import java.net.URI;

public interface Pingable {
    void ping();
    URI pingEndpoint();
    String name();
}
