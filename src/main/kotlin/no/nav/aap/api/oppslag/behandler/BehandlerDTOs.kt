package no.nav.aap.api.oppslag.behandler

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler.BehandlerKategori
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler.BehandlerType
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler.KontaktInformasjon

@JsonIgnoreProperties(ignoreUnknown = true)
data class BehandlerDTO(
        val type: BehandlerType,
        val kategori: BehandlerKategori,
        val behandlerRef: String,
        val fnr: Fødselsnummer?,
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val orgnummer: OrgNummer?,
        val kontor: String?,
        val adresse: String?,
        val postnummer: String,
        val poststed: String?,
        val telefon: String?) {

    fun tilBehandler() = RegistrertBehandler(type, kategori, Navn(fornavn, mellomnavn, etternavn),
            KontaktInformasjon(kontor, orgnummer,
                    Adresse(adresse, null, null, PostNummer(postnummer, poststed)),
                    telefon))
}

data class AnnenBehandler(val type: BehandlerType,
                          val kategori: BehandlerKategori?,
                          val navn: Navn,
                          val kontaktinformasjon: KontaktInformasjon)

data class RegistrertBehandler(val type: BehandlerType,
                               val kategori: BehandlerKategori,
                               val navn: Navn,
                               val kontaktinformasjon: KontaktInformasjon,
                               val erRegistrertFastlegeRiktig: Boolean? = null) {
    enum class BehandlerType {
        FASTLEGE,
        SYKMELDER,
    }

    enum class BehandlerKategori {
        LEGE,
        FYSIOTERAPEUT,
        KIROPRAKTOR,
        MANUELLTERAPEUT,
        TANNLEGE
    }

    data class KontaktInformasjon(val kontor: String?,
                                  val orgnummer: OrgNummer?,
                                  val adresse: Adresse?,
                                  var telefon: String?)
}