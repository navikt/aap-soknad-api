package no.nav.aap.api

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
import no.nav.aap.util.AccessorUtil
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
            AccessorUtil.init()
            applicationStartup = BufferingApplicationStartup(4096)
        }
    }