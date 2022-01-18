package no.nav.aap.api.oppslag.pdl

import no.nav.aap.api.felles.Navn
import org.springframework.stereotype.Component

@Component
class PDLClient(private val pdl: PDLWebClientAdapter) : PDLOperations {
    override fun person() = pdl.navn()?.let { Person(Navn(it.fornavn, it.mellomnavn, it.etternavn), null) }
    override fun toString() = "${javaClass.simpleName} [pdl=$pdl]"
}