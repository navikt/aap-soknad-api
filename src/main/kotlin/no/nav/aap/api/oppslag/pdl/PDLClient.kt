package no.nav.aap.api.oppslag.pdl

import org.springframework.stereotype.Component

@Component
class PDLClient(private val pdl: PDLWebClientAdapter) {
    fun søkerUtenBarn() = pdl.søker(false)
    fun søkerMedBarn() = pdl.søker(true)

    override fun toString() = "${javaClass.simpleName} [pdl=$pdl]"
}