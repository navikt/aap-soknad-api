package no.nav.aap.api.oppslag.arbeidsforhold

import no.nav.aap.api.oppslag.arbeidsforhold.OrganisasjonConfig.Companion.ORGANISASJON
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class OrganisasjonClientBeanConfig {

    @Bean
    @Qualifier(ORGANISASJON)
    fun organisasjonWebClient(builder: WebClient.Builder, cfg: OrganisasjonConfig, tokenXFilter: TokenXFilterFunction) =
        builder
            .baseUrl("${cfg.baseUri}")
            .filter(tokenXFilter)
            .build()

    @Bean
    fun organisasjonHealthIndicator(a: OrganisasjonWebClientAdapter) =
        object : AbstractPingableHealthIndicator(a) {}
}