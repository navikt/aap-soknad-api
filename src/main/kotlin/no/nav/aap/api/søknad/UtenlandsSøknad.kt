package no.nav.aap.api.søknad

import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Periode

data class UtenlandsSøknad(val land: CountryCode, val periode: Periode)