package no.nav.aap.api.oppslag.fastlege

import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.Periode
import java.time.LocalDate

data class FastlegeDTO(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val fnr: String?,
        val herId: Int?,
        val foreldreEnhetHerId: Int?,
        val helsepersonellregisterId: Int?,
        val pasient: Pasient,
        val fastlegekontor: Fastlegekontor,
        val pasientforhold: Pasientforhold) {

    data class Pasient(
            val fornavn: String?,
            val mellomnavn: String?,
            val etternavn: String?,
            val fnr: String?)

    data class Fastlegekontor(
            val navn: String?,
            val besoeksadresse: Adresse?,
            val postadresse: Adresse?,
            val telefon: String?,
            val epost: String?,
            val orgnummer: String?) {
        data class Adresse(
                val adresse: String?,
                val postnummer: String?,
                val poststed: String?)
    }
    data class Pasientforhold(
            val fom: LocalDate,
            val tom: LocalDate)
}

data class Fastlege(val navn: Navn?) // TODO mer