package no.nav.aap.api.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.time.Duration.ofMillis
import org.springframework.boot.actuate.metrics.AutoTimer
import org.springframework.stereotype.Component

@Component
class Metrikker(private val registry: MeterRegistry) {
    fun inc(navn: String, vararg tags: String) = Counter.builder(navn)
        .tags(*tags.map(String::lowercase).toTypedArray())
        .register(registry)
        .increment()

    companion object {
        const val SØKNAD = "søknad"
        const val ETTERSENDING = "ettersending"
        const val MOTTATT = "mottatt"
        const val MANGLENDE = "manglende"
        const val INNSENDING = "innsending"
        const val TYPE = "type"
        const val YRKESSKADE = "yrkesskade"
        const val VEDLEGG = "soknad.vedlegg"
        const val MELLOMLAGRING = "soknad.mellomlagring"
        const val MELLOMLAGRING_EXPIRED = "soknad.expired"
        const val SØKNADER = "soknad.innsendte"
        const val STATUS = "status"
        const val KOMPLETT = "komplett"
        const val INKOMPLETT = "inkomplett"
        const val KOMPLETTMEDVEDLEGG = "vedleggkomplett"
        const val VEDLEGGINKOMPLETT = "vedlegginkomplett"

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
}