package no.nav.aap.api.oppslag.pdl

import no.nav.aap.api.felles.Navn
import org.springframework.stereotype.Component

@Component
class PDLClient(private val pdl: PDLWebClientAdapter) : PDLOperations {
    override fun person() = pdl.person()?.let { Person(Navn(it.fornavn, it.mellomnavn, it.etternavn), it.fødselsdato) }
    override fun toString() = "${javaClass.simpleName} [pdl=$pdl]"
}