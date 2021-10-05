package no.nav.aap.api;

import java.net.URI;

public interface PingEndpointAware extends Pingable {
     URI pingEndpoint();
    String name();

}
