package no.nav.aap.api.søknad

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.IdentType.FNR
import java.time.LocalDate

data class StandardSøknadKafka(val ident: Ident, val fødselsdato: LocalDate?) {
    constructor(fnr: Fødselsnummer, fødselsdato: LocalDate?) : this(Ident(fnr), fødselsdato)
    val id  = ident.verdi
}