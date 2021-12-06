package no.nav.aap.api.søknad

import io.micrometer.core.instrument.MeterRegistry
import no.nav.aap.api.felles.UtenlandsSøknadKafka
import org.springframework.stereotype.Component

@Component
class SøknadMetrics(private val meterRegistry: MeterRegistry) {
    private val TAG_LAND = "land"
    private val TAG_VARIGHET = "varighet"
    private val COUNTER_SØKNAD_UTLAND_MOTTATT = "aap_soknad_utland_mottatt"

    fun increment(søknad: UtenlandsSøknadKafka) {
        meterRegistry.counter(COUNTER_SØKNAD_UTLAND_MOTTATT,
                TAG_LAND, søknad.land.alpha3,
                TAG_VARIGHET, søknad.periode.varighetDager().toString())
            .increment()
    }
}