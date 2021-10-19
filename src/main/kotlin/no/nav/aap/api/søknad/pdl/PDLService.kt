package no.nav.aap.api.søknad.pdl

import no.nav.aap.api.søknad.domain.Navn
import org.springframework.stereotype.Service

@Service
class PDLService(private val pdl: PDLWebClientAdapter) : PdlOperations {
    override fun navn(): Navn? {
        val n: PDLNavn? = pdl.navn()
        if (n != null) {
            return  Navn(n.fornavn, n.mellomnavn, n.etternavn)
        }
        return null
    }

    override fun toString() = "${javaClass.simpleName} [pdl=$pdl]"

}