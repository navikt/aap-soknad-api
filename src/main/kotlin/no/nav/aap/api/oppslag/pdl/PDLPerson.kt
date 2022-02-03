package no.nav.aap.api.oppslag.pdl

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate


data class PDLWrappedPerson(val navn: Set<PDLNavn>,
                            @JsonProperty("foedsel") val fødsel: Set<PDLFødsel>, val bostedsadresse: List<PDLBostedadresse>) {
    val active = PDLPerson(navn.first(), fødsel.firstOrNull(), bostedsadresse.firstOrNull()?.vegadresse)
}

data class PDLNavn(val fornavn: String, val mellomnavn: String?, val etternavn: String)

data class PDLPerson(val navn: PDLNavn, val fødsel: PDLFødsel?, val vegadresse: PDLVegadresse?) {
    val fornavn = navn.fornavn
    val mellomnavn = navn.mellomnavn
    val etternavn = navn.etternavn
    val fødselsdato = fødsel?.fødselsdato
}

data class PDLFødsel(@JsonProperty("foedselsdato") val fødselsdato: LocalDate?)

data class PDLBostedadresse(val vegadresse: PDLVegadresse)

data class PDLVegadresse(val adressenavn: String, val husbokstav: String?, val husnummer: String?, val postnummer: String)

data class PDLBarn(@JsonProperty("foedsel") val  fødselsdato: Set<PDLFødsel>,
                   val navn: Set<PDLNavn>,
                   @JsonProperty("kjoenn") val kjønn: Set <PDLKjønn>,
                   @JsonProperty("adressebeskyttelse") val beskyttelse: Set<PDLAdresseBeskyttelse>,
                   @JsonProperty("doedsfall") val dødsfall: Set<PDLDødsfall>,
                   val forelderBarnRelasjon: Set<PDLForelderBarnRelasjon>)

data class PDLDødsfall(@JsonProperty("doedsdato") val dødsdato: LocalDate)


enum class PDLAdresseBeskyttelse {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT
}

enum class PDLKjønn {
    MANN,
    KVINNE,
    UKJENT
}
data class PDLForelderBarnRelasjon(@JsonProperty("relatertPersonsIdent") val id: String,
                                   val relatertPersonsrolle: PDLRelasjonsRolle,
                                   @JsonProperty("minRolleForPerson") val minRolle: PDLRelasjonsRolle)
enum class PDLRelasjonsRolle {
        BARN,
        MOR,
        FAR,
        MEDMOR
}