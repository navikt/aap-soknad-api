package no.nav.aap.api

import no.nav.boot.conditionals.Cluster.currentCluster
import no.nav.boot.conditionals.Cluster.profiler
import no.nav.boot.conditionals.EnvUtil.DEV
import no.nav.boot.conditionals.EnvUtil.DEV_FSS
import no.nav.boot.conditionals.EnvUtil.PROD
import no.nav.boot.conditionals.EnvUtil.PROD_FSS
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@EnableOAuth2Client(cacheEnabled = true)
@ConfigurationPropertiesScan
@EnableRetry
@EnableKafka
@EnableCaching
class AAPSøknadApiApplication

fun main(args: Array<String>) {
    runApplication<AAPSøknadApiApplication>(*args) {
        with(currentCluster().clusterName()) {
            if (contains(DEV)) {
                setDefaultProperties(mapOf("onprem" to DEV_FSS))
            }
            if (contains(PROD)) {
                setDefaultProperties(mapOf("onprem" to PROD_FSS))
            }
        }

        setAdditionalProfiles(*profiler())

        applicationStartup = BufferingApplicationStartup(4096)
    }
}