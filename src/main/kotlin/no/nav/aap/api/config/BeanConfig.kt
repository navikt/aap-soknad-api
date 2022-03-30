package no.nav.aap.api.config

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import no.nav.aap.rest.HeadersToMDCFilter
import no.nav.aap.rest.MDCPropagatingFilterFunction
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.rest.tokenx.TokenXJacksonModule
import no.nav.aap.util.AuthContext
import no.nav.aap.util.StartupInfoContributor
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.zalando.problem.jackson.ProblemModule
import org.springframework.web.context.request.RequestContextListener





@Configuration
class BeanConfig(@Value("\${spring.application.name}") private val applicationName: String) {

    @Bean
    fun customizer() = Jackson2ObjectMapperBuilderCustomizer {
        b -> b.modules(ProblemModule(), JavaTimeModule(), TokenXJacksonModule(), KotlinModule.Builder().build())
    }

    @Bean
    fun authContext(h: TokenValidationContextHolder) = AuthContext(h)

    @Bean
    fun openAPI(p: BuildProperties) =
        OpenAPI()
            .info(Info()
                .title("AAP søknadmottak")
                .description("Mottak av søknader")
                .version(p.version)
                .license(License()
                    .name("MIT")
                    .url("https://www.nav.no")))
    
    @Bean
    fun configMatcher() = object :  ClientConfigurationPropertiesMatcher {}

    @Bean
    @Order(HIGHEST_PRECEDENCE + 1)
    fun mdcPropagator(h: TokenValidationContextHolder) = MDCPropagatingFilterFunction(h)

    @Bean
    @Order(HIGHEST_PRECEDENCE + 2)
    fun tokenXFilterFunction(configs: ClientConfigurationProperties,
                             service: OAuth2AccessTokenService,
                             matcher: ClientConfigurationPropertiesMatcher,
                             ctx: AuthContext) = TokenXFilterFunction(configs, service, matcher, ctx)

    @Bean
    fun startupInfoContributor(ctx: ApplicationContext) = StartupInfoContributor(ctx)

    @Bean
    fun headersToMDCFilterRegistrationBean() =
        FilterRegistrationBean(HeadersToMDCFilter(applicationName))
            .apply {
                urlPatterns = listOf("/*")
                setOrder(HIGHEST_PRECEDENCE)
            }
}