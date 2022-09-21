package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.Filtype.PDFA
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.VariantFormat.*
import no.nav.aap.util.Constants.AAP
import no.nav.aap.util.MDCUtil
import java.util.*

data class Journalpost(
        val tittel: String,
        val avsenderMottaker: AvsenderMottaker,
        val bruker: Bruker,
        val dokumenter: List<Dokument?> = mutableListOf(),
        val eksternReferanseId: UUID = MDCUtil.callIdAsUUID(),
        val tilleggsopplysninger: List<Tilleggsopplysning> = mutableListOf(),
        val kanal: String = KANAL,
        val journalposttype: String = INNGÅENDE,
        val tema: String = AAP.uppercase()) {



data class Tilleggsopplysning(val nokkel: String, val verdi: String)

data class Dokument private constructor(val tittel: String?, val brevkode: String? = null, val dokumentVarianter: List<DokumentVariant?>) {
    constructor(type: SkjemaType = STANDARD, dokumentVarianter: List<DokumentVariant?>) : this(type.tittel,type.kode, dokumentVarianter)
    constructor(tittel: String? = null,dokumentVariant: DokumentVariant) : this(tittel,null,listOf(dokumentVariant))

}

data class DokumentVariant private constructor(val filtype: String, val fysiskDokument: String, val variantformat: String) {
    constructor(filtype: Filtype = PDFA, fysiskDokument: String, variantformat: VariantFormat = ARKIV) :this(filtype.name,fysiskDokument, variantformat.name)
        override fun toString() = "${javaClass.simpleName} [filtype=$filtype,variantformat=$variantformat,fysiskDokument=${fysiskDokument.length} bytes]"
    enum class VariantFormat {
        ORIGINAL,
        ARKIV,
        FULLVERSJON
    }

    enum class Filtype {
        PDFA,
        JPEG,
        PNG,
        JSON
    }
}
data class Bruker(val id: Fødselsnummer, val idType: String = ID_TYPE)
data class AvsenderMottaker(val id: Fødselsnummer,  val navn: String?,val idType: String = ID_TYPE,)

    companion object{
        private const val INNGÅENDE = "INNGAAENDE"
        private const val KANAL = "NAV_NO"
        private const val ID_TYPE = "FNR"
    }
}