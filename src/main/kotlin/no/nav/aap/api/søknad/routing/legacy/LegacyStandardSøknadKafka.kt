package no.nav.aap.api.søknad.routing.legacy

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.routing.legacy.IdentType.FNR
import java.time.LocalDate
@Deprecated("Kun for enkel testing")
data class LegacyStandardSøknadKafka(val ident: Ident, val fødselsdato: LocalDate?) {
    constructor(fnr: Fødselsnummer, fødselsdato: LocalDate?) : this(Ident(fnr), fødselsdato)

    val id = ident.verdi
}
@Deprecated("Kun for enkel testing")
data class Ident(val type: IdentType = FNR, val verdi: String) {
    constructor(fnr: Fødselsnummer) : this(FNR, fnr.fnr)
}

@Deprecated("Kun for enkel testing")
enum class IdentType { FNR }