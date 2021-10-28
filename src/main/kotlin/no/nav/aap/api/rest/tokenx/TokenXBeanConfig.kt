package no.nav.aap.api.rest.tokenx

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI


@Configuration
class TokenXBeanConfig {
    @Bean
    fun configMatcher() = object : TokenXConfigMatcher {
        override fun findProperties(configs: ClientConfigurationProperties, uri: URI): ClientProperties? {
            return configs.registration[uri.host.split("\\.".toRegex()).toTypedArray()[0]]
        }
    }
}