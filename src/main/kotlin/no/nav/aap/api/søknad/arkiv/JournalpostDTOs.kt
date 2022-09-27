package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.Filtype.PDFA
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.VariantFormat.ARKIV
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.MDCUtil.callIdAsUUID
import java.util.*

data class Journalpost(
        val tittel: String,
        val avsenderMottaker: AvsenderMottaker,
        val bruker: Bruker,
        val dokumenter: List<Dokument>,
        val eksternReferanseId: UUID = callIdAsUUID(),
        val kanal: String = KANAL,
        val journalposttype: String = INNGÅENDE,
        val tema: String = AAP.uppercase()) {

    data class Dokument private constructor(val tittel: String?, val brevkode: String? = null, val varianter: List<DokumentVariant?>) {
        constructor(varianter: List<DokumentVariant?>, type: SkjemaType = STANDARD) : this(type.tittel, type.kode, varianter)
        constructor(tittel: String? = null, variant: DokumentVariant) : this(tittel, null, listOf(variant))
    }

    data class DokumentVariant private constructor(val filtype: String, val fysiskDokument: String, val format: String) {
        constructor(fysiskDokument: String, format: VariantFormat = ARKIV, filtype: Filtype = PDFA) : this(filtype.name, fysiskDokument, format.name)
        override fun toString() = "${javaClass.simpleName} [filtype=$filtype,format=$format,fysiskDokument=${fysiskDokument.length} bytes]"
        enum class VariantFormat { ORIGINAL, ARKIV, FULLVERSJON }
        enum class Filtype { PDFA, JPEG, PNG, JSON }
    }

    data class Bruker(val id: Fødselsnummer, val idType: String = ID_TYPE)
    data class AvsenderMottaker(val id: Fødselsnummer, val navn: String?, val idType: String = ID_TYPE)


    companion object {
        private const val INNGÅENDE = "INNGAAENDE"
        private const val KANAL = "NAV_NO"
        private const val ID_TYPE = "FNR"
    }
}