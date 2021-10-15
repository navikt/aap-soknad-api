package no.nav.aap.api.søknad.pdl

import no.nav.aap.api.søknad.domain.Navn

interface PdlOperations {
    fun navn(): Navn?
}