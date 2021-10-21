package no.nav.aap.api.pdl

import no.nav.aap.api.oppslag.Navn
import org.springframework.stereotype.Service

@Service
class PDLClient(private val pdl: PDLWebClientAdapter) : PDLOperations {
    override fun navn(): Navn? {
        return pdl.navn()?.let { Navn(it.fornavn, it.mellomnavn, it.etternavn) }
    }
    override fun toString() = "${javaClass.simpleName} [pdl=$pdl]"
}