package no.nav.aap.api.oppslag

import no.nav.aap.api.felles.Fødselsnummer

data class Søker(val fnr: Fødselsnummer, val navn: Navn?)
data class Navn(val fornavn: String?,val mellomnavn: String?,val etternavn: String?)