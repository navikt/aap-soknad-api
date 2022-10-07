package no.nav.aap.api.søknad.model

import java.time.LocalDate
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn

data class Søker(val navn: Navn,
                 val fnr: Fødselsnummer,
                 val adresse: Adresse? = null,
                 val fødseldato: LocalDate? = null,
                 val barn: List<Barn> = emptyList()) {

    data class Barn(val navn: Navn, val fødseldato: LocalDate? = null)
}