package no.nav.aap.api.oppslag.pdl

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate


data class PDLWrappedPerson(val navn: Set<PDLNavn>,
                            @JsonProperty("foedsel") val fødsel: Set<PDLFødsel>) {
    val active = PDLPerson(navn.first(), fødsel.firstOrNull())
}

data class PDLNavn(val fornavn: String,
                   val mellomnavn: String?,
                   val etternavn: String)


data class PDLPerson(val navn: PDLNavn, val fødsel: PDLFødsel?) {
    val fornavn = navn.fornavn
    val mellomnavn = navn.mellomnavn
    val etternavn = navn.etternavn
    val fødselsdato = fødsel?.fødselsdato
}

data class PDLFødsel(@JsonProperty("foedselsdato") val fødselsdato: LocalDate?)