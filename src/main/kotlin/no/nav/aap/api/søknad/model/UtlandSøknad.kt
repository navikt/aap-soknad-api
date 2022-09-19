package no.nav.aap.api.søknad.model

import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Periode

data class UtlandSøknad(val land: CountryCode, val periode: Periode)