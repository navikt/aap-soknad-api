package no.nav.aap.api.søknad.virussjekk

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder
import no.nav.aap.api.søknad.virussjekk.VirusScanConfig.Companion.VIRUS
import no.nav.aap.health.AbstractPingableHealthIndicator

@Configuration
@ConditionalOnProperty("$VIRUS.enabled", havingValue = "true")
class VirusScanBeanConfg {
    @Bean
    @Qualifier(VIRUS)
    fun webClientVirusScqn(b: Builder, cfg: VirusScanConfig) = b.baseUrl("${cfg.baseUri}").build()

    @Bean
    fun virusHealthIndicator(adapter: VirusScanWebClientAdapter) = object : AbstractPingableHealthIndicator(adapter) {}
}