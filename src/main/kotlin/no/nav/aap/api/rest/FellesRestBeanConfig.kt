package no.nav.aap.api.rest

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import no.nav.aap.api.rest.tokenx.TokenXModule
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.boot.actuate.web.trace.servlet.HttpTraceFilter
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.zalando.problem.jackson.ProblemModule
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest


@Configuration
class FellesRestBeanConfig {
    @Bean
    fun customizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { b: Jackson2ObjectMapperBuilder ->
            b.modules(ProblemModule(), JavaTimeModule(), TokenXModule(), KotlinModule())
        }
    }
    @Bean
    @ConditionalOnDevOrLocal
    fun httpTraceRepository(): HttpTraceRepository {
        return InMemoryHttpTraceRepository()
    }
    @Bean
    fun swagger(): OpenAPI? {
        return OpenAPI()
            .info(
                Info().title("AAP søknadmotaker")
                    .description("Mottak av søknader")
                    .version("v0.0.1")
                    .license(License().name("MIT").url("http://nav.no"))
            )
    }

    @ConditionalOnDevOrLocal
    class ActuatorIgnoringTraceRequestFilter(repository: HttpTraceRepository?, tracer: HttpExchangeTracer?) :
        HttpTraceFilter(repository, tracer) {
        @Throws(ServletException::class)
        override fun shouldNotFilter(request: HttpServletRequest): Boolean {
            return request.servletPath.contains("actuator") || request.servletPath.contains("swagger")
        }
    }

}