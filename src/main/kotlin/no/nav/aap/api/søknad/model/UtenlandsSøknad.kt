package no.nav.aap.api.søknad.model

import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.felles.Søker
import no.nav.aap.api.felles.UtenlandsSøknadKafka

data class UtenlandsSøknad(val land: CountryCode, val periode: Periode) {
    fun berikSøknad(søker: Søker) = UtenlandsSøknadKafka(søker, land, periode)
}