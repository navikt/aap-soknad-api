package no.nav.aap.api.oppslag.pdl

import no.nav.aap.api.felles.Navn

interface PDLOperations {
    fun navn(): Navn?
}