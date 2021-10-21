package no.nav.aap.api.pdl

import no.nav.aap.api.oppslag.Navn

interface PdlOperations {
    fun navn(): Navn?
}