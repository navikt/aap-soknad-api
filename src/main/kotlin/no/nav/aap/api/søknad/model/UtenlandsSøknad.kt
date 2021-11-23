package no.nav.aap.api.søknad.model

import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.oppslag.Navn
import no.nav.aap.api.oppslag.Søker

data class UtenlandsSøknadView(val land: CountryCode, val periode: Periode)
fun UtenlandsSøknadView.toKafkaObject(søker: Søker) = UtenlandsSøknadKafka(søker.fnr.fnr, land, periode,søker.navn)
data class UtenlandsSøknadKafka(val fnr: String, val land: CountryCode, val periode: Periode,val navn: Navn?)