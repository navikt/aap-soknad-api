package no.nav.aap.api.oppslag.pdl

import no.nav.aap.api.oppslag.Navn

interface PDLOperations {
    fun navn(): Navn?
}