package no.nav.aap.api.oppslag.pdl

import org.springframework.stereotype.Component

@Component
class PDLClient(private val pdl: PDLWebClientAdapter) : PDLOperations {
    override fun søker( medBarn: Boolean) = pdl.søker(medBarn)
    override fun toString() = "${javaClass.simpleName} [pdl=$pdl]"
}