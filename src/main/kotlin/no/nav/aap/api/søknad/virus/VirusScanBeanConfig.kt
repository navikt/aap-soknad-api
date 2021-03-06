package no.nav.aap.api.søknad.virus

import no.nav.aap.api.søknad.virus.VirusScanConfig.Companion.VIRUS
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder
import java.net.URI

@Configuration
class VirusScanBeanConfg {
    @Bean
    @Qualifier(VIRUS)
    fun webClientVirusScqn(b: Builder, cfg: VirusScanConfig) = b.baseUrl("${cfg.baseUri}").build()

    @Bean
    fun virusHealthIndicator(a: VirusScanWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}

@ConfigurationProperties(VIRUS)
@ConstructorBinding
data class VirusScanConfig(@DefaultValue(BASE_URI) val uri: URI,
                           @DefaultValue("true") val enabled: Boolean) : AbstractRestConfig(uri, "", VIRUS, enabled) {

    companion object {
        const val VIRUS = "virus"
        private const val BASE_URI = "http://clamav.clamav.svc.cluster.local/scan"
    }
}