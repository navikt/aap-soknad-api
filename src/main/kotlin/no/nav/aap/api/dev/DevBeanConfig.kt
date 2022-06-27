package no.nav.aap.api.dev

import no.nav.aap.rest.ActuatorIgnoringTraceRequestFilter
import no.nav.aap.util.LoggerUtil
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

val log = LoggerUtil.getLogger(DevBeanConfig::class.java)

@Bean
fun httpTraceRepository(env: Environment): HttpTraceRepository {
    log.info("DB ${env.getProperty("db.username")}")
    log.info("DB ${env.getProperty("db.passwors")}")
    return InMemoryHttpTraceRepository()
}

@Bean
fun actuatorIgnoringTraceRequestFilter(repo: HttpTraceRepository, tracer: HttpExchangeTracer) =
    ActuatorIgnoringTraceRequestFilter(repo, tracer)