package no.nav.aap.api.søknad.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Periode
import no.nav.aap.api.søknad.arkiv.DokumentVariant
import no.nav.aap.api.søknad.arkiv.Filtype.JSON
import no.nav.aap.api.søknad.arkiv.VariantFormat.ORIGINAL
import no.nav.aap.util.StringExtensions.toEncodedJson

data class UtlandSøknad(val land: CountryCode, val periode: Periode) {
    fun somJsonVariant(mapper: ObjectMapper) = DokumentVariant(JSON, toEncodedJson(mapper), ORIGINAL)
}