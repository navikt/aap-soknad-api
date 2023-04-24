package no.nav.aap.api.oppslag.person

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.api.søknad.model.Innsending

@Component
@Observed(contextualName = "PDL")
class PDLClient(private val adapter: PDLWebClientAdapter) {
    fun søkerUtenBarn() = adapter.søker(false)
    fun tilVikafossen(innsending: Innsending) =
        runCatching {
            adapter.harBeskyttedeFosterbarn(innsending.søknad.andreBarn.map { it.barn })
        }.getOrElse { false }
    fun søkerMedBarn() = adapter.søker(true)
    override fun toString() = "${javaClass.simpleName} [pdl=$adapter]"
}