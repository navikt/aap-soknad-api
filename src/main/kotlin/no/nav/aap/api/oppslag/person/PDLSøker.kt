package no.nav.aap.api.oppslag.person

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.person.PDLSøker.PDLBostedadresse
import no.nav.aap.api.oppslag.person.PDLSøker.PDLBostedadresse.PDLVegadresse
import no.nav.aap.api.oppslag.person.PDLSøker.PDLForelderBarnRelasjon
import no.nav.aap.api.oppslag.person.PDLSøker.PDLForelderBarnRelasjon.PDLRelasjonsRolle.BARN
import no.nav.aap.api.oppslag.person.PDLSøker.PDLFødsel

data class PDLWrappedSøker(val navn: Set<PDLNavn>,
                           @JsonProperty("foedsel") val fødsel: Set<PDLFødsel>,
                           val bostedsadresse: List<PDLBostedadresse> = emptyList(),
                           val adressebeskyttelse: Set<PDLGradering> = emptySet(),
                           val forelderBarnRelasjon: Set<PDLForelderBarnRelasjon>?) {
    val active = PDLSøker(navn.first(), fødsel.firstOrNull(), bostedsadresse.firstOrNull()?.vegadresse, adressebeskyttelse,
            forelderBarnRelasjon?.filter {
                it.relatertPersonsrolle == BARN
            } ?: emptyList())
}

enum class PDLAdresseBeskyttelse {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT
}
data class PDLGradering(val gradering: PDLAdresseBeskyttelse)
data class PDLNavn(val fornavn: String, val mellomnavn: String?, val etternavn: String)

data class PDLSøker(val navn: PDLNavn,
                    val fødsel: PDLFødsel?,
                    val vegadresse: PDLVegadresse?,
                    val adressebeskyttelse: Set<PDLGradering> = emptySet(),
                    val forelderBarnRelasjon: List<PDLForelderBarnRelasjon>) {

    data class PDLForelderBarnRelasjon(val relatertPersonsIdent: String?,
                                       val relatertPersonsrolle: PDLRelasjonsRolle,
                                       val minRolleForPerson: PDLRelasjonsRolle) {
        enum class PDLRelasjonsRolle {
            BARN,
            MOR,
            FAR,
            MEDMOR
        }
    }

    data class PDLFødsel(@JsonProperty("foedselsdato") val fødselsdato: LocalDate?)

    data class PDLBostedadresse(val vegadresse: PDLVegadresse?) {
        data class PDLVegadresse(val adressenavn: String,
                                 val husbokstav: String?,
                                 val husnummer: String?,
                                 val postnummer: String)
    }
}

data class PDLBolkBarn(val ident:String, val code: String, @JsonProperty("person") val barn: PDLBarn) {
    data class PDLBarn(@JsonProperty("foedsel") val fødselsdato: Set<PDLFødsel>,
                       val navn: Set<PDLNavn>,
                       val adressebeskyttelse: Set<PDLGradering>?,
                       @JsonProperty("doedsfall") val dødsfall: Set<PDLDødsfall>?,
                       val fnr: Fødselsnummer? = null) {

        data class PDLDødsfall(@JsonProperty("doedsdato") val dødsdato: LocalDate)

    }
}