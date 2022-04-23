package no.nav.aap.api.søknad.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.neovisionaries.i18n.CountryCode
import no.nav.aap.api.felles.Periode
import java.util.Base64

data class UtlandSøknad(val land: CountryCode, val periode: Periode) {
    fun toEncodedJson(mapper: ObjectMapper) = Base64.getEncoder()
        .encodeToString(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this).toByteArray())
}