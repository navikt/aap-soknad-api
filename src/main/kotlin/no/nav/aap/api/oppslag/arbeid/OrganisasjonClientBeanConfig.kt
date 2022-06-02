package no.nav.aap.api.oppslag.arbeid

import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.util.Constants.ORGANISASJON
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class OrganisasjonClientBeanConfig(val cfg: OrganisasjonConfig) {

    @Bean
    @Qualifier(ORGANISASJON)
    fun organisasjonWebClient(builder: Builder) =
        builder
            .baseUrl("${cfg.baseUri}")
            .build()

    @Bean
    fun organisasjonHealthIndicator(a: OrganisasjonWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}