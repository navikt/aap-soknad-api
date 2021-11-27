package no.nav.aap.api.søknad.model

import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.felles.Søker

data class UtenlandsSøknadView(val land: CountryCode, val periode: Periode)

fun UtenlandsSøknadView.toKafkaObject(søker: Søker) = UtenlandsSøknadKafka(søker, land, periode)
data class UtenlandsSøknadKafka(val søker: Søker, val land: CountryCode, val periode: Periode)