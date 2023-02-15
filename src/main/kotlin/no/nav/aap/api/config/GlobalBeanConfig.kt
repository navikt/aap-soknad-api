package no.nav.aap.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.ServiceOptions
import com.google.cloud.storage.StorageOptions
import com.nimbusds.jwt.JWTClaimNames.JWT_ID
import io.micrometer.core.aop.CountedAspect
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.netty.channel.ChannelOption
import io.netty.channel.ChannelOption.*
import io.netty.channel.ConnectTimeoutException
import io.netty.handler.logging.LogLevel.TRACE
import io.netty.handler.timeout.ReadTimeoutException
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.TimeoutException
import io.netty.handler.timeout.WriteTimeoutException
import io.netty.handler.timeout.WriteTimeoutHandler
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP
import java.io.IOException
import java.time.Duration
import java.time.Duration.*
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS
import java.util.function.Consumer
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import no.nav.aap.api.config.Metrikker.Companion.metricsWebClientFilterFunction
import no.nav.aap.health.Pingable
import no.nav.aap.rest.AbstractWebClientAdapter.Companion.correlatingFilterFunction
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
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.security.token.support.client.core.OAuth2ClientException
import no.nav.security.token.support.client.core.http.OAuth2HttpClient
import no.nav.security.token.support.client.core.http.OAuth2HttpRequest
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.apache.commons.text.StringEscapeUtils.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.MethodParameter
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.*
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import org.threeten.bp.Duration.ofMillis
import org.zalando.problem.jackson.ProblemModule
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL
import reactor.util.retry.Retry.fixedDelay

@Configuration
class GlobalBeanConfig(@Value("\${spring.application.name}") private val applicationName: String)  {
    val log = getLogger(javaClass)

    @Bean
    @Primary
    fun gcpStorageRetrySettings(@Value("\${mellomlagring.timeout:2500}") timeoutMs: Int) =
         ServiceOptions.getDefaultRetrySettings().toBuilder()
            .setInitialRetryDelay(ofMillis(400))
            .setMaxRetryDelay(ofMillis(900))
            .setRetryDelayMultiplier(1.5)
            .setMaxAttempts(5)
            .setTotalTimeout(ofMillis(timeoutMs.toLong()))
            .build()
    @Bean
    @Primary
    fun gcpStorage(retrySettings: RetrySettings) =  StorageOptions
        .newBuilder()
        .setRetrySettings(retrySettings)
        .build()
        .service
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
    fun webClientCustomizer(client: HttpClient, registry: MeterRegistry) =
        WebClientCustomizer { b ->
            b.clientConnector(ReactorClientHttpConnector(client))
                .filter(correlatingFilterFunction(applicationName))
                .filter(metricsWebClientFilterFunction(registry,"webclient"))
        }

    @ConditionalOnNotProd
    @Bean
    fun notProdHttpClient() = httpClient().wiretap(javaClass.name, TRACE, TEXTUAL)

    @ConditionalOnProd
    @Bean
    fun prodHttpClient() =httpClient()


    private fun httpClient() = HttpClient.create() /*
        .doOnConnected {
            it.addHandlerFirst(ReadTimeoutHandler(30, SECONDS))
            it.addHandlerFirst(WriteTimeoutHandler(30, SECONDS))
        }
        .responseTimeout(ofSeconds(30))
        .option(CONNECT_TIMEOUT_MILLIS, 10000) */

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

        val log = getLogger(javaClass)

        override fun ping(): Map<String, String> {
            log.info("Helsesjekker ${cfg.topics()}")
            return cfg.topics().mapIndexedNotNull { ix, topic -> innslag(topic,ix)}
                .associateBy({ it.first }, { it.second })
        }

        private fun innslag(topic: String, ix: Int): Pair<String,String>?{
             runCatching {
                with(admin.describeTopics(topic).values.first()) {
                     return Pair("topic-$ix", "${name()} (${partitions().count()} partisjoner")
                 }
             }.recover {
                 return Pair(topic, it.message ?: it.javaClass.simpleName)
             }
            return null
        }

        abstract class AbstractKafkaConfig(val name: String, val isEnabled: Boolean) {
            abstract fun topics(): List<String>
        }
    }

    @Bean
    fun retryingOAuth2HttpClient(b: WebClient.Builder) = RetryingWebClientOAuth2HttpClient(b.build())

    class RetryingWebClientOAuth2HttpClient(private val client: WebClient) : OAuth2HttpClient {

        private val log = getLogger(javaClass)

        override fun post(req: OAuth2HttpRequest) =
            with(req) {
                client.post()
                    .uri(tokenEndpointUrl)
                    .headers { Consumer<HttpHeaders> { it.putAll(oAuth2HttpHeaders.headers()) } }
                    .bodyValue(LinkedMultiValueMap<String, String>().apply { setAll(formParameters) })
                    .retrieve()
                    .bodyToMono<OAuth2AccessTokenResponse>()
                    .retryWhen(retry())
                    .onErrorMap {
                        when (it) {
                            is TimeoutException -> OAuth2ClientException("${it.javaClass.simpleName} timeout, skal føre til retry",it)
                            is ConnectTimeoutException -> OAuth2ClientException("Connect timeout, skal føre til retry",it)
                            else -> it
                        }
                    }
                    .doOnSuccess { log.trace("Token endpoint returnerte OK") }
                    .block()
                    ?: throw OAuth2ClientException("Ingen respons (null) fra token endpoint $tokenEndpointUrl")
            }
        private fun retry() =
            fixedDelay(3, Duration.ofMillis(100))
                .filter { e -> e is OAuth2ClientException}
                .doBeforeRetry { s -> log.info("Retry kall mot token endpoint feilet med  ${s.failure().message} for ${s.totalRetriesInARow() + 1} gang, prøver igjen",s.failure()) }
                .onRetryExhaustedThrow { _, spec ->  spec.failure().also { log.warn("Retry mot token endpoint gir opp etter ${spec.totalRetriesInARow()} forsøk") } }

    }
}