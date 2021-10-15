package no.nav.aap.api.søknad.domain

import com.neovisionaries.i18n.CountryCode

data class UtenlandsSøknad(val land: CountryCode, val periode: Periode)