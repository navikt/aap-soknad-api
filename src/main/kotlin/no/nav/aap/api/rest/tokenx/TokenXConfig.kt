package no.nav.aap.api.rest.tokenx

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.net.URI


@Configuration
class TokenXConfig {
    @Bean
    fun configMatcher() = object : TokenXConfigMatcher {
        override fun findProperties(configs: ClientConfigurationProperties, uri: URI): ClientProperties? {
            return configs.registration[uri.host.split("\\.".toRegex()).toTypedArray()[0]]
        }
    }
    @Bean
    fun customizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { b: Jackson2ObjectMapperBuilder ->
            b.mixIn(OAuth2AccessTokenResponse::class.java, IgnoreUnknownMixin::class.java)
        }
    }
    @Bean
    @ConditionalOnDevOrLocal
    fun httpTraceRepository(): HttpTraceRepository? {
        return InMemoryHttpTraceRepository()
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface IgnoreUnknownMixin
}