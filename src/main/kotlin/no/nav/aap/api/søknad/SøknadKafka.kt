package no.nav.aap.api.søknad

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.IdentType.FNR
import java.time.LocalDate

data class SøknadKafka(val ident: Ident, val fødselsdato: LocalDate?) {
    constructor(fnr: Fødselsnummer, fødselsdato: LocalDate?) : this(Ident(fnr), fødselsdato)
    val id  = ident.verdi
}

data class Ident(val type: IdentType = FNR, val verdi: String) {
    constructor(fnr: Fødselsnummer) : this(FNR, fnr.fnr)

}

enum class IdentType {
    FNR
}