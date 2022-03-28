package no.nav.aap.api.oppslag.krr

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.aap.api.oppslag.krr.Målform.Companion
import no.nav.aap.api.oppslag.krr.Målform.NB


@JsonIgnoreProperties(ignoreUnknown = true)
class Kontaktinformasjon(@JsonProperty("spraak") val  målform: Målform? = Målform.standard())

enum class Målform {
    NB,NN,EN;
    companion object {
        fun standard() = NB
    }
}