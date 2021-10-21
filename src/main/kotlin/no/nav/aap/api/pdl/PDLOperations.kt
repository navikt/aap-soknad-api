package no.nav.aap.api.pdl

import no.nav.aap.api.oppslag.Navn

interface PDLOperations {
    fun navn(): Navn?
}