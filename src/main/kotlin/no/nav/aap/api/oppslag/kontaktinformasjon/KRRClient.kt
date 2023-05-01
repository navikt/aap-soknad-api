package no.nav.aap.api.oppslag.kontaktinformasjon

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.kontaktinformasjon.KRRConfig.Companion.KRR

@Component
@Observed(contextualName = KRR)
class KRRClient(private val adapter : KRRWebClientAdapter) {

    fun kontaktInfo() = adapter.kontaktInformasjon()
}