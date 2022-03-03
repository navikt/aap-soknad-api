package no.nav.aap.api.søknad.model

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import java.time.LocalDate

data class Søker(val navn: Navn, val fødseldato: LocalDate?, val barn: List<Barn?>)

class Søknad

data class Barn(val fnr: Fødselsnummer, val navn: Navn,val fødseldato: LocalDate?)