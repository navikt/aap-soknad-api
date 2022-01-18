package no.nav.aap.api.oppslag.pdl

import no.nav.aap.api.søknad.model.Person

interface PDLOperations {
    fun person(): Person?
}