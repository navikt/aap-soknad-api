package no.nav.aap.api.oppslag.pdl

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate


data class PDLWrappedPerson(val navn: Set<PDLNavn>, @JsonProperty("foedsel") val fødsel: Set<PDLFødsel>)
data class PDLNavn(val fornavn: String,
                   val mellomnavn: String?,
                   val etternavn: String)

data class PDLPerson(val navn: PDLNavn?, val fødsel: PDLFødsel?)

data class PDLFødsel(@JsonProperty("foedselsdato") val fødselsdato: LocalDate)