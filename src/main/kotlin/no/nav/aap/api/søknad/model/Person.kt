package no.nav.aap.api.søknad.model

import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import java.time.LocalDate

data class Søker(val navn: Navn,
                 val fødselsnummer: Fødselsnummer,
                 val adresse: Adresse?,
                 val fødseldato: LocalDate?,
                 val barn: List<Barn?>? = null)

data class Barn(val fnr: Fødselsnummer,
                val navn: Navn? = null,
                val fødseldato: LocalDate? = null)