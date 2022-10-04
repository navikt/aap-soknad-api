package no.nav.aap.api.oppslag.arbeid

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.Constants.ORGANISASJON
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
@ConditionalOnProperty("$ORGANISASJON.enabled", havingValue = "true")
internal class OrganisasjonClientBeanConfig {

    @Bean
    @Qualifier(ORGANISASJON)
    fun organisasjonWebClient(builder: Builder, cfg: OrganisasjonConfig) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    fun organisasjonHealthIndicator(a: OrganisasjonWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}