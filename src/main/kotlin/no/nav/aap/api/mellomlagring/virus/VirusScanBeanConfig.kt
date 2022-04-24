package no.nav.aap.api.mellomlagring.virus

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
internal class VirusScanBeanConfg {
    @Bean
    @Qualifier("virus")
    fun webClientVirusScqn(builder: Builder, cfg: VirusScanConfig)  =
        builder.baseUrl("${cfg.baseUri}").build()
}