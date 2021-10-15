package no.nav.aap.api.søknad.pdl

import no.nav.aap.api.søknad.health.AbstractPingableHealthIndicator
import org.springframework.stereotype.Component

@Component
class PDLHealthIndicator(adapter: PDLWebClientAdapter) : AbstractPingableHealthIndicator(adapter!!)