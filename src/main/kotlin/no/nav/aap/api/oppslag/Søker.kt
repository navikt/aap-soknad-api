package no.nav.aap.api.oppslag

import no.nav.aap.api.felles.Fødselsnummer

data class Søker(val fnr: Fødselsnummer, val navn: Navn?)