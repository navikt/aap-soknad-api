package no.nav.aap.api.oppslag.behandler

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component

@Component
@Observed(contextualName = "Behandler")
class BehandlerClient(private val adapter: BehandlerWebClientAdapter) {
    fun behandlerInfo() = adapter.behandlerInfo()
}