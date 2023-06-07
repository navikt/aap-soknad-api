package no.nav.aap.api.oppslag.person

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.person.Søker.Barn

@Component
class PDLClient(private val adapter : PDLWebClientAdapter) {
    fun søkerUtenBarn() = adapter.søker()
    fun harBeskyttetBarn(barn : List<Barn>) = adapter.harBeskyttetBarn(barn)
    fun søkerMedBarn() = adapter.søker(true)

    override fun toString() = "${javaClass.simpleName} [pdl=$adapter]"
}