package no.nav.aap.api.søknad

import io.micrometer.core.instrument.MeterRegistry
import no.nav.aap.api.felles.Periode
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class SøknadMetrics(private val meterRegistry: MeterRegistry) {

    private val log = LoggerUtil.getLogger(javaClass)

    private val TAG_LAND = "land"
    private val TAG_VARIGHET = "varighet"

    private val COUNTER_SØKNAD_UTLAND_MOTTATT = "aap_soknad_utland_mottatt"

    fun incrementSøknadUtlandMottatt(land: String, periode: Periode) {
        runCatching {
            meterRegistry.counter(
                    COUNTER_SØKNAD_UTLAND_MOTTATT,
                    TAG_LAND,
                    land,
                    TAG_VARIGHET,
                    periode.varighetDager().toString()
                                 ).increment()
        }.onFailure {
            log.debug("incrementSøknadUtlandMottatt feilet", it)
        }
    }

}
