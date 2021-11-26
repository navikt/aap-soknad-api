package no.nav.aap.api.søknad

import io.micrometer.core.instrument.MeterRegistry
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class SøknadMetrics(private val meterRegistry: MeterRegistry) {

    private val log = LoggerUtil.getLogger(javaClass)

    private val SØKNAD_UTLAND = "utland"

    private val COUNTER_SØKNAD_MOTTATT = "aap_soknad_mottatt"

    fun incrementSøknadUtlandMottatt(land: String, periode: Periode) {
        runCatching {
            meterRegistry.counter(
                COUNTER_SØKNAD_MOTTATT,
                SØKNAD_UTLAND,
                land,
                periode.varighetDager().toString()
            ).increment()
        }.onFailure {
            log.debug("incrementSøknadUtlandMottatt feilet", it)
        }
    }

}
