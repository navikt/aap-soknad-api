package no.nav.aap.api.søknad.tokenx;

import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;

import java.net.URI;
import java.util.Optional;

 interface TokenXConfigMatcher {
    Optional<ClientProperties> findProperties(ClientConfigurationProperties configs, URI uri);
}
