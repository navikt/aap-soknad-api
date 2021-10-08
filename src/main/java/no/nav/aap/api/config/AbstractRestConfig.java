package no.nav.aap.api.config;

import java.net.URI;

import static no.nav.aap.api.util.URIUtil.uri;

public class AbstractRestConfig {

    private static final String IDPORTEN = "idporten";
    private final URI baseUri;
    private final String pingPath;
    private final boolean enabled;
    public static final String ISSUER = IDPORTEN;

    protected AbstractRestConfig(URI baseUri, String pingPath, boolean enabled) {
        this.baseUri = baseUri;
        this.pingPath = pingPath;
        this.enabled = enabled;
    }

    public URI pingEndpoint() {
        return uri(baseUri, pingPath);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public String name() {
        return baseUri.getHost();
    }
}
