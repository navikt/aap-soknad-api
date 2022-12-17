package no.nav.aap.api.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.time.Duration.ofMillis
import no.nav.aap.api.felles.SkjemaType.STANDARD
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

    private const val STATUS = "status"
    private const val KOMPLETT = "komplett"
    private const val INKOMPLETT = "inkomplett"



    fun komplettSøknad(registry: MeterRegistry) = registrerSøknad(registry,KOMPLETT)

    fun inkomplettSøknad(registry: MeterRegistry) = registrerSøknad(registry,INKOMPLETT)

    private fun registrerSøknad(registry: MeterRegistry, status: String) = registry.counter(SØKNADER,"type", STANDARD.name.lowercase(),STATUS,status).increment()


    fun metricsWebClientFilterFunction(registry: MeterRegistry, name: String, autoTimer: AutoTimer = AutoTimerHistogram()) = MetricsWebClientFilterFunction(
            registry,
            DefaultWebClientExchangeTagsProvider(),
            name,
            autoTimer)

}
internal class AutoTimerHistogram : AutoTimer {
    override fun apply(builder: Timer.Builder) {
        builder.serviceLevelObjectives(
                ofMillis(100),
                ofMillis(500),
                ofMillis(800),
                ofMillis(1000),
                ofMillis(1200))
            .publishPercentileHistogram(true)
            .minimumExpectedValue(ofMillis(100))
            .maximumExpectedValue(ofMillis(10000))
    }


}