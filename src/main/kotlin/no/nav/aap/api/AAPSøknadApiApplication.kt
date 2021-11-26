package no.nav.aap.api

import no.nav.boot.conditionals.Cluster.profiler
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.retry.annotation.EnableRetry


@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@EnableOAuth2Client(cacheEnabled = true)
@ConfigurationPropertiesScan
@EnableRetry
@EnableKafka
class AAPSøknadApiApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(AAPSøknadApiApplication::class.java)
                .profiles(*profiler())
                .main(AAPSøknadApiApplication::class.java)
                .run(*args)
        }
    }
}