package no.nav.aap.api.rest.tokenx

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import java.net.URI

@FunctionalInterface
interface TokenXConfigMatcher {
    fun findProperties(configs: ClientConfigurationProperties, uri: URI): ClientProperties?
}