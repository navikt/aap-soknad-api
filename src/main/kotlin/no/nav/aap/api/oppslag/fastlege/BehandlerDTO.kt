package no.nav.aap.api.oppslag.fastlege

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.OrgNummer

data class BehandlerDTO(
        val type: BehandlerType,
        val behandlerRef: String,
        val fnr: Fødselsnummer?,
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val orgnummer: OrgNummer?,
        val kontor: String?,
        val adresse: String?,
        val postnummer: String?,
        val poststed: String?,
        val telefon: String?)

enum class BehandlerType {
    FASTLEGE
}

data class Fastlege(val navn: Navn) // TODO mer


fun BehandlerDTO.tilFastlege() = Fastlege(Navn(fornavn,mellomnavn,etternavn)) // TODO