package no.nav.aap.api.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.time.Duration.ofMillis
import no.nav.aap.api.felles.SkjemaType.STANDARD
import org.springframework.boot.actuate.metrics.AutoTimer
import org.springframework.boot.actuate.metrics.web.reactive.client.DefaultWebClientExchangeTagsProvider
import org.springframework.boot.actuate.metrics.web.reactive.client.MetricsWebClientFilterFunction
import org.springframework.stereotype.Component

@Component
class Metrikker(private val registry: MeterRegistry) {
    fun inc(navn: String, vararg tags: String) = Counter.builder(navn)
        .tags(*tags)
        .register(registry)
        .increment()

    companion object {
        const val ETTERSENDTE = "soknad.vedlegg.ettersendte"
        const val INNSENDTE = "soknad.vedlegg.innsendte"
        const val MANGLENDE = "soknad.vedlegg.manglende"
        const val MELLOMLAGRING = "soknad.mellomlagring"
        const val MELLOMLAGRING_EXPIRED = "soknad.expired"
        const val SØKNADER = "soknad.innsendte"
        fun metricsWebClientFilterFunction(registry: MeterRegistry, name: String, autoTimer: AutoTimer = AutoTimerHistogram()) = MetricsWebClientFilterFunction(
                registry,
                DefaultWebClientExchangeTagsProvider(),
                name,
                autoTimer)
    }
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