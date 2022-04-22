package no.nav.aap.api.oppslag.pdl

import org.springframework.stereotype.Component

@Component
class PDLClient(private val a: PDLWebClientAdapter) {
    fun søkerUtenBarn() = a.søker(false)
    fun søkerMedBarn() = a.søker(true)
    override fun toString() = "${javaClass.simpleName} [pdl=$a]"
}