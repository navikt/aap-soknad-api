package no.nav.aap.api

import io.micrometer.context.ThreadLocalAccessor
import io.micrometer.context.ContextRegistry.getInstance
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder.getRequestAttributes
import org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes
import org.springframework.web.context.request.RequestContextHolder.setRequestAttributes
import reactor.core.publisher.Hooks
import no.nav.boot.conditionals.Cluster.Companion.profiler
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation

@SpringBootApplication(exclude= [ErrorMvcAutoConfiguration::class])
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@EnableOAuth2Client(cacheEnabled = true)
@ConfigurationPropertiesScan
@EnableKafka
@EnableCaching
@EnableJpaAuditing
@EnableSpringDataWebSupport
@EnableScheduling
class AAPSøknadApiApplication

    fun main(args: Array<String>) {
        runApplication<AAPSøknadApiApplication>(*args) {
            setAdditionalProfiles(*profiler())
            Hooks.enableAutomaticContextPropagation()
            getInstance().apply {
                registerThreadLocalAccessor(RequestAttributesAccessor())
            }
            applicationStartup = BufferingApplicationStartup(4096)
        }
    }

private class RequestAttributesAccessor : ThreadLocalAccessor<RequestAttributes> {

    override fun key() = RequestAttributesAccessor::class.java.name

    override fun getValue() = getRequestAttributes()

    override fun setValue(value : RequestAttributes) = setRequestAttributes(value)

    override fun reset() = resetRequestAttributes()
}