package no.nav.aap.api.oppslag.pdl

import org.springframework.stereotype.Component

@Component
class PDLClient(private val adapter: PDLWebClientAdapter) {
    fun søkerUtenBarn() = adapter.søker(false)
    fun søkerMedBarn() = adapter.søker(true)
    override fun toString() = "${javaClass.simpleName} [pdl=$adapter]"
}