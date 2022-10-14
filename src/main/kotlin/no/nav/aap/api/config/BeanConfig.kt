package no.nav.aap.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.nimbusds.jwt.JWTClaimNames.JWT_ID
import io.micrometer.core.aop.CountedAspect
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.netty.handler.logging.LogLevel.TRACE
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP
import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import no.nav.aap.health.Pingable
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
import no.nav.aap.rest.ActuatorIgnoringTraceRequestFilter
import no.nav.aap.rest.HeadersToMDCFilter
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.rest.tokenx.TokenXJacksonModule
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.toMDC
import no.nav.aap.util.StartupInfoContributor
import no.nav.aap.util.StringExtensions.toJson
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.boot.conditionals.ConditionalOnProd
import no.nav.boot.conditionals.EnvUtil.*
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.apache.commons.text.StringEscapeUtils.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer
import org.springframework.boot.actuate.trace.http.HttpTrace
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.MediaType.*
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import org.zalando.problem.jackson.ProblemModule
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL

@Configuration
class BeanConfig(@Value("\${spring.application.name}") private val applicationName: String) {

    @Bean
    fun countedAspect(registry: MeterRegistry) = CountedAspect(registry)
    @Bean
    fun timedAspect(registry: MeterRegistry) = TimedAspect(registry)

    @Bean
    fun customizer() = Jackson2ObjectMapperBuilderCustomizer { b ->
        b.modules(ProblemModule(),
                JavaTimeModule(),
                TokenXJacksonModule(),
                KotlinModule.Builder().build())
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
    fun webClientCustomizer(client: HttpClient) =
        WebClientCustomizer { b ->
            b.clientConnector(ReactorClientHttpConnector(client))
                .filter(correlatingFilterFunction(applicationName))
        }

    @ConditionalOnNotProd
    @Bean
    fun notProdHttpClient() = HttpClient.create().wiretap(javaClass.name, TRACE, TEXTUAL)

    @ConditionalOnProd
    @Bean
    fun prodHttpClient() = HttpClient.create()

    @Bean
    @ConditionalOnNotProd
    fun actuatorIgnoringTraceRequestFilter(repo: HttpTraceRepository, tracer: HttpExchangeTracer) =
        ActuatorIgnoringTraceRequestFilter(repo, tracer)

    @ConditionalOnNotProd
    class HttpTraceRepository(private val mapper: ObjectMapper) : InMemoryHttpTraceRepository() {
        private val log = getLogger(javaClass)
        override fun add(trace: HttpTrace) {
            runCatching {
                log.trace(CONFIDENTIAL, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(trace))
                super.add(trace)
            }.getOrNull()
        }
    }

    class JTIFilter(private val ctx: AuthContext) : Filter {
        @Throws(IOException::class, ServletException::class)
        override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
            toMDC(JWT_ID, ctx.getClaim(IDPORTEN, JWT_ID), "Ingen JTI")
            chain.doFilter(request, response)
        }
    }

    @ControllerAdvice
    class LoggingResponseBodyAdvice(private val mapper: ObjectMapper) : ResponseBodyAdvice<Any?> {

        private val log = getLogger(javaClass)
        override fun beforeBodyWrite(body: Any?,
                                     returnType: MethodParameter,
                                     contentType: MediaType,
                                     selectedConverterType: Class<out HttpMessageConverter<*>>,
                                     request: ServerHttpRequest,
                                     response: ServerHttpResponse): Any? {
            if (contentType in listOf(APPLICATION_JSON, parseMediaType("application/problem+json"))) {
                log.trace(CONFIDENTIAL, "Response body for ${request.uri} er ${body?.toJson(mapper)}")
            }
            return body
        }

        override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>) = true
    }

    abstract class AbstractKafkaHealthIndicator(private val admin: KafkaAdmin,
                                                private val bootstrapServers: List<String>,
                                                private val cfg: AbstractKafkaConfig) : Pingable {
        override fun isEnabled() = cfg.isEnabled
        override fun pingEndpoint() = "$bootstrapServers"
        override fun name() = cfg.name

        override fun ping() =
            admin.describeTopics(*cfg.topics().toTypedArray()).entries
                .withIndex()
                .associate {
                    with(it) {
                        "topic-${index}" to "${value.value.name()} (${value.value.partitions().count()} partisjoner)"
                    }
                }

        abstract class AbstractKafkaConfig(val name: String, val isEnabled: Boolean) {
            abstract fun topics(): List<String>
        }
    }

}