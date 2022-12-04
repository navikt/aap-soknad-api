package no.nav.aap.api.oppslag.konto

import java.time.Duration.*
import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.health.AbstractPingableHealthIndicator
import no.nav.aap.rest.tokenx.TokenXFilterFunction
import no.nav.aap.util.LoggerUtil.getLogger
import org.hibernate.secure.spi.IntegrationException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient.Builder
import reactor.util.retry.Retry
import reactor.util.retry.Retry.*

@Configuration
class KontoClientBeanConfig {

    val log = getLogger(javaClass)

    @Bean
    @Qualifier(KONTO)
    fun kontoRetry(cfg: KontoConfig): Retry =
        fixedDelay(3, ofMillis(100))
            .filter { e -> e is IntegrationException }
            .doBeforeRetry { s -> log.warn("Retry kall mot ${cfg.baseUri} grunnet exception ${s.failure().javaClass.name} og melding ${s.failure().message} for ${s.totalRetriesInARow() + 1} gang, prøver igjen") }

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