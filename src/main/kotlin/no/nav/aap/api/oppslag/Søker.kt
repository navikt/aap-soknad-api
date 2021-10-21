package no.nav.aap.api.oppslag

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import no.nav.aap.api.felles.Fødselsnummer

data class Søker(val fnr: Fødselsnummer, val navn: Navn?)
@JsonPropertyOrder("fornavn", "mellomnavn", "etternavn")
data class Navn(val fornavn: String?,val mellomnavn: String?,val etternavn: String?)