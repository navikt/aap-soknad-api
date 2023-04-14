package no.nav.aap.api.oppslag.person

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component

@Component
@Observed(name = "PDL")
class PDLClient(private val adapter: PDLWebClientAdapter) {
    fun søkerUtenBarn() = adapter.søker(false)
    fun søkerMedBarn() = adapter.søker(true)
    override fun toString() = "${javaClass.simpleName} [pdl=$adapter]"
}