package no.nav.aap.api.oppslag.krr

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class KontaktinformasjonDTO(@JsonProperty("spraak") val  målform: Målform? = Målform.standard(),
                                 val reservert: Boolean? = null,
                                 val kanVarsles: Boolean? = false,
                                 val epostadresse: String? = null,
                                 val mobiltelefonnummer: String?  = null)

enum class Målform {
    NB,NN,EN;
    companion object {
        fun standard() = NB
    }
}