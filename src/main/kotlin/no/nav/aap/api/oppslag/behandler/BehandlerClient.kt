package no.nav.aap.api.oppslag.behandler

import org.springframework.stereotype.Component

@Component
class BehandlerClient(private val a: BehandlerWebClientAdapter) {
    fun behandlerinfo() = a.behandlere()
}