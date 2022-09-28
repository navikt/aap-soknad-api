package no.nav.aap.api.dev

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.sun.jndi.ldap.LdapPoolManager.trace
import no.nav.aap.rest.ActuatorIgnoringTraceRequestFilter
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer
import org.springframework.boot.actuate.trace.http.HttpTrace
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnDevOrLocal
class DevBeanConfig


@Bean
fun actuatorIgnoringTraceRequestFilter(repo: HttpTraceRepository, tracer: HttpExchangeTracer) =
    ActuatorIgnoringTraceRequestFilter(repo, tracer)

@Bean
fun httpTraceRepository(mapper: ObjectMapper) = object : InMemoryHttpTraceRepository() {
        private  val log = getLogger(javaClass)
        override fun add(trace: HttpTrace)  {
            runCatching {
                log.trace(CONFIDENTIAL,mapper.writerWithDefaultPrettyPrinter().writeValueAsString(trace))
                super.add(trace)
            }.getOrNull()
        }
}