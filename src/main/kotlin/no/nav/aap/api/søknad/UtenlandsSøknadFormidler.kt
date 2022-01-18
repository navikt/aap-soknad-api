package no.nav.aap.api.søknad

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.model.UtenlandsSøknadView

interface UtenlandsSøknadFormidler {
    fun formidle(fnr: Fødselsnummer, søknad: UtenlandsSøknadView)
}