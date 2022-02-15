package no.nav.aap.api.oppslag.fastlege

import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.Periode

data class FastlegeDTO(
        val fornavn: String,
        val mellomnavn: String,
        val etternavn: String,
        val fnr: String?,
        val herId: Int?,
        val helsepersonellregisterId: Int?,
        val fastlegekontor: Fastlegekontor,
        val pasientforhold: Periode,
        val foreldreEnhetHerId: Int? = null,
        val pasient: Pasient? = null,
        val gyldighet: Periode,
        val relasjon: Relasjon)

data class Fastlegekontor (
        val navn: String,
        val besoeksadresse: Adresse?,
        val postadresse: Adresse?,
        val telefon: String,
        val epost: String,
        val orgnummer: String?)

data class Adresse(
        val adresse: String,
        val postnummer: String,
        val poststed: String)

data class Pasient(
        val fnr: String,
        val fornavn: String,
        val mellomnavn: String? = null,
        val etternavn: String)

data class Relasjon(
        val kodeVerdi: String,
        val kodeTekst: String)

data class Fastlege(val navn: Navn?) // TODO mer