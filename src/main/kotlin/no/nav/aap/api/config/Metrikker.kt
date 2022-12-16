package no.nav.aap.api.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.time.Duration
import org.springframework.boot.actuate.metrics.AutoTimer
import org.springframework.boot.actuate.metrics.web.reactive.client.DefaultWebClientExchangeTagsProvider
import org.springframework.boot.actuate.metrics.web.reactive.client.MetricsWebClientFilterFunction

object Metrikker {
    const val ETTERSENDTE = "soknad.vedlegg.ettersendte"
    const val INNSENDTE = "soknad.vedlegg.innsendte"
    const val MANGLENDE = "soknad.vedlegg.manglende"
    const val MELLOMLAGRING = "soknad.mellomlagring"
    const val MELLOMLAGRING_EXPIRED = "soknad.expired"
    const val SØKNADER = "soknad.innsendte"
    const val AVSLUTTET_BESKJED = "soknad.beskjed.avsluttet"
    const val OPPRETTET_BESKJED = "soknad.beskjed.opprettet"
    const val AVSLUTTET_OPPGAVE = "soknad.oppgave.avsluttet"
    const val OPPRETTET_OPPGAVE = "soknad.oppgave.opprettet"
    const val AVSLUTTET_UTKAST = "soknad.utkast.avsluttet"
    const val OPPRETTET_UTKAST = "soknad.utkast.opprettet"
    const val OPPDATERT_UTKAST = "soknad.utkast.oppdatert"

    fun metricsWebClientFilterFunction(registry: MeterRegistry, name: String, autoTimer: AutoTimer = AutoTimerHistogram()) = MetricsWebClientFilterFunction(
            registry,
            DefaultWebClientExchangeTagsProvider(),
            name,
            AutoTimerHistogram())

}
class AutoTimerHistogram : AutoTimer {
    override fun apply(builder: Timer.Builder) {
        builder
            .serviceLevelObjectives(
                    Duration.ofMillis(100),
                    Duration.ofMillis(500),
                    Duration.ofMillis(800),
                    Duration.ofMillis(1000),
                    Duration.ofMillis(1200))
            .publishPercentileHistogram(true)
            .minimumExpectedValue(Duration.ofMillis(100))
            .maximumExpectedValue(Duration.ofMillis(10000))
    }
}