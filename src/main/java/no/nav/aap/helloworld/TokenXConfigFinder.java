package no.nav.aap.helloworld;

import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;

import java.net.URI;

public interface TokenXConfigFinder {
        ClientProperties findProperties(ClientConfigurationProperties configs, URI uri);
}
