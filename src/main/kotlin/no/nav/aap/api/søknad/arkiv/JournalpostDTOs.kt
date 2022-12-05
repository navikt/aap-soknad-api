package no.nav.aap.api.søknad.arkiv

import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.Filtype.PDFA
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.VariantFormat.ARKIV
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.MDCUtil.callIdAsUUID

data class Journalpost(
        val tittel: String,
        val avsenderMottaker: AvsenderMottaker,
        val bruker: Bruker,
        val dokumenter: List<Dokument>,
        val eksternReferanseId: UUID = callIdAsUUID(),
        val kanal: String = KANAL,
        val journalposttype: String = INNGÅENDE,
        val tema: String = AAP.uppercase()) {

    data class Dokument private constructor(val tittel: String?,
                                            val brevkode: String? = null,
                                            val dokumentVarianter: List<DokumentVariant>) {
        constructor(dokumentVarianter: List<DokumentVariant>, type: SkjemaType = STANDARD) : this(type.tittel,
                type.kode,
                dokumentVarianter)

        constructor(tittel: String? = null,  brevkode: String?,variant: DokumentVariant) : this(tittel, brevkode, listOf(variant))
    }

    data class DokumentVariant private constructor(val filtype: String,
                                                   val fysiskDokument: String,
                                                   val variantformat: String) {
        constructor(fysiskDokument: String, variantformat: VariantFormat = ARKIV, filtype: Filtype = PDFA) : this(
                filtype.name,
                fysiskDokument,
                variantformat.name)

        override fun toString() = "${javaClass.simpleName} [filtype=$filtype,format=$variantformat,fysiskDokument=${fysiskDokument.length} bytes]"

        enum class VariantFormat { ORIGINAL, ARKIV, FULLVERSJON }

        enum class Filtype { PDFA, JPEG, PNG, JSON }
    }

    data class Bruker(val id: Fødselsnummer, val idType: String = ID_TYPE)
    data class AvsenderMottaker private constructor(val id: Fødselsnummer, val navn: String?, val idType: String = ID_TYPE) {
        constructor (id: Fødselsnummer, navn: Navn) : this(id, navn.navn)
    }

    companion object {
        private const val INNGÅENDE = "INNGAAENDE"
        private const val KANAL = "NAV_NO"
        private const val ID_TYPE = "FNR"
    }

    override fun toString() = "${javaClass.simpleName} [filtype=$tittel,dokumenter=$dokumenter,eksternReferanseId=$eksternReferanseId]"

}