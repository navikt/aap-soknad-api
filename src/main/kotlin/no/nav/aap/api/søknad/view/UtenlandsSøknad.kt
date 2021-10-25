package no.nav.aap.api.søknad.view

import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Periode

data class UtenlandsSøknadView(
    val land: CountryCode,
    val periode: Periode
)

fun UtenlandsSøknadView.toKafkaObject(fnr: String) = UtenlandsSøknadKafka(
    fnr, land, periode
)

data class UtenlandsSøknadKafka(
    val fnr: String,
    val land: CountryCode,
    val periode: Periode
)
