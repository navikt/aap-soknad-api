package no.nav.aap.api.dev

import no.nav.aap.rest.ActuatorIgnoringTraceRequestFilter
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
@ConditionalOnDevOrLocal
class DevBeanConfig

@Bean
fun httpTraceRepository(env: Environment): HttpTraceRepository = InMemoryHttpTraceRepository()

@Bean
fun actuatorIgnoringTraceRequestFilter(repo: HttpTraceRepository, tracer: HttpExchangeTracer) =
    ActuatorIgnoringTraceRequestFilter(repo, tracer)