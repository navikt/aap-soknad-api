package no.nav.aap.api.oppslag.krr

import com.fasterxml.jackson.annotation.JsonProperty


class Kontaktinformasjon(@JsonProperty("spraak") val  målform: Målform)

enum class Målform {
    NB,NN,EN;
    companion object {
        fun standard() =NB
    }
}