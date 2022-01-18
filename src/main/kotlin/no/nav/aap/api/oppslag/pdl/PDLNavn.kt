package no.nav.aap.api.oppslag.pdl

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate


data class PDLWrappedPerson(val navn: Set<PDLNavn>, val foedsel: Set<PDLFødsel>)
data class PDLNavn(val fornavn: String?,
                   val mellomnavn: String?,
                   val etternavn: String?)

class PDLFødsel(@JsonProperty("foedselsdato") fødselsdato: LocalDate)