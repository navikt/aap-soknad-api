package no.nav.aap.api.oppslag.konto

import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
@ConditionalOnProperty("$KONTO.enabled", havingValue = "true")
class KontoClientBeanConfig {

    @Qualifier(KONTO)
    @Bean
    fun kontoWebClient(b: Builder, cfg: KontoConfig, tokenX: TokenXFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(tokenX)
            .build()

    @Bean
    fun kontoHealthIndicator(a: KontoWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}