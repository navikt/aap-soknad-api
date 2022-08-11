package no.nav.aap.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.nimbusds.jwt.JWTClaimNames.JWT_ID
import com.vladmihalcea.hibernate.type.util.ObjectMapperSupplier
import io.netty.handler.logging.LogLevel.TRACE
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.rest.HeadersToMDCFilter
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.rest.tokenx.TokenXJacksonModule
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.aap.util.MDCUtil.toMDC
import no.nav.aap.util.StartupInfoContributor
import no.nav.boot.conditionals.EnvUtil.isDevOrLocal
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.zalando.problem.jackson.ProblemModule
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL
import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Configuration
class BeanConfig(@Value("\${spring.application.name}") private val applicationName: String) {

    @Bean
    fun customizer() = Jackson2ObjectMapperBuilderCustomizer { b ->
        b.modules(ProblemModule(), JavaTimeModule(), TokenXJacksonModule(), KotlinModule.Builder().build())
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
            .components(Components()
                .addSecuritySchemes("bearer-key",
                        SecurityScheme().type(HTTP).scheme("bearer")
                            .bearerFormat("JWT")))

    @Bean
    fun configMatcher() =
        object : ClientConfigurationPropertiesMatcher {}

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

    @Bean
    fun jtiToMDCFilterRegistrationBean(ctx: AuthContext) =
        FilterRegistrationBean(JTIFilter(ctx))
            .apply {
                urlPatterns = listOf("/*")
                setOrder(LOWEST_PRECEDENCE)
            }

    @Bean
    fun webClientCustomizer(env: Environment) =
        WebClientCustomizer { b ->
            b.clientConnector(ReactorClientHttpConnector(client(env)))
                .filter(correlatingFilterFunction(applicationName))
        }

    private fun client(env: Environment) =
        if (isDevOrLocal(env))
            HttpClient.create().wiretap(javaClass.canonicalName, TRACE, TEXTUAL)
        else HttpClient.create()
}

class JTIFilter(private val ctx: AuthContext) : Filter {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        toMDC(JWT_ID, ctx.getClaim(IDPORTEN, JWT_ID), "Ingen JTI")
        chain.doFilter(request, response)
    }
}

class HibernateObjectMapperSupplier : ObjectMapperSupplier {
    override fun get() = ObjectMapperHolder.mapper
}

@Component
class ObjectMapperHolder(objectMapper: ObjectMapper) {
    init {
        mapper = objectMapper
    }

    companion object {
        lateinit var mapper: ObjectMapper
    }
}

@Component
class ObjectMapperDependencyFixer : BeanFactoryPostProcessor {
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        beanFactory.getBeanDefinition("entityManagerFactory").apply {
            setDependsOn(*(dependsOn ?: emptyArray()) + "objectMapperHolder")
        }
    }
}