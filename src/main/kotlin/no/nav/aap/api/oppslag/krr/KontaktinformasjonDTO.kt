package no.nav.aap.api.oppslag.krr

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class KontaktinformasjonDTO(@JsonAlias("spraak") val målform: Målform? = Målform.standard(),
                                 val reservert: Boolean? = null,
                                 val kanVarsles: Boolean? = false,
                                 @JsonAlias("epostadresse") val epost: String? = null,
                                 @JsonAlias("mobiltelefonnummer")val mobil: String?  = null)

enum class Målform {
    NB,NN,EN;
    companion object {
        fun standard() = NB
    }
}