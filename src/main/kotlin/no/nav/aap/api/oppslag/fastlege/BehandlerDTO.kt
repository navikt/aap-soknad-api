package no.nav.aap.api.oppslag.fastlege

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.oppslag.fastlege.Fastlege.BehandlerType
import no.nav.aap.api.oppslag.fastlege.Fastlege.KontaktInformasjon

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


data class Fastlege(val type: BehandlerType,val navn: Navn, val kontaktinformasjon: KontaktInformasjon) {
    enum class BehandlerType {
        FASTLEGE
    }
    data class KontaktInformasjon(val behandlerRef: String, val kontor: String?, val orgnummer: OrgNummer?, val adresse: String?, val postnr: String?, val poststed: String?, var telefon: String?)
}
fun BehandlerDTO.tilFastlege() = Fastlege(type,Navn(fornavn,mellomnavn,etternavn),
        KontaktInformasjon(behandlerRef,kontor,orgnummer,adresse,postnummer,poststed,telefon))