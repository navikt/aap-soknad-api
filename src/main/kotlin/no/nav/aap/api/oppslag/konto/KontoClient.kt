package no.nav.aap.api.oppslag.konto

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO

@Component
@Observed(contextualName = KONTO)
class KontoClient(private val adapter : KontoWebClientAdapter) {

    fun kontoInfo() = adapter.kontoInfo()
}