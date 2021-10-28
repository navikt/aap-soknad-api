package no.nav.aap.api.rest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.zalando.problem.jackson.ProblemModule


@Configuration
class FellesRestBeanConfig {
    @Bean
    fun customizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { b: Jackson2ObjectMapperBuilder ->
            b.modules(ProblemModule())
            b.mixIn(OAuth2AccessTokenResponse::class.java, IgnoreUnknownMixin::class.java)
        }
    }
    @Bean
    @ConditionalOnDevOrLocal
    fun httpTraceRepository(): HttpTraceRepository {
        return InMemoryHttpTraceRepository()
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface IgnoreUnknownMixin


}