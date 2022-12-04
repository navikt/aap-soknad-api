package no.nav.aap.api.oppslag.konto

import java.time.Duration.*
import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder
import reactor.util.retry.Retry.*

@Configuration
class KontoClientBeanConfig {

    val log = getLogger(javaClass)

    @Qualifier(KONTO)
    @Bean
    fun kontoWebClient(b: Builder, cfg: KontoConfig, tokenX: TokenXFilterFunction) =
        b.baseUrl("${cfg.baseUri}")
            .filter(tokenX)
            .build()

    @Bean
    @ConditionalOnProperty("$KONTO.enabled", havingValue = "true")
    fun kontoHealthIndicator(a: KontoWebClientAdapter) = object : AbstractPingableHealthIndicator(a) {}
}