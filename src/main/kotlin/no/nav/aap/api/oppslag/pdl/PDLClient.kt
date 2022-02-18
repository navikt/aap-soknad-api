package no.nav.aap.api.oppslag.pdl

import org.springframework.stereotype.Component

@Component
class PDLClient(private val pdl: PDLWebClientAdapter)  {
    fun søker( medBarn: Boolean = false) = pdl.søker(medBarn)
    override fun toString() = "${javaClass.simpleName} [pdl=$pdl]"
}