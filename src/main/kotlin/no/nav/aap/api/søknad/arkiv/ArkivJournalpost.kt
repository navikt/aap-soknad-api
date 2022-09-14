package no.nav.aap.api.søknad.arkiv
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.arkiv.ArkivJournalpost.Dokument.DokumentVariant.Filtype.PDFA
import no.nav.aap.api.søknad.arkiv.ArkivJournalpost.Dokument.DokumentVariant.VariantFormat.ARKIV
import no.nav.aap.util.Constants.AAP
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE

data class ArkivJournalpost(
        val journalposttype: String = INNGÅENDE,
        val tema: String = AAP.uppercase(),
        val kanal: String = KANAL,
        val tittel: String,
        val avsenderMottaker: AvsenderMottaker,
        val bruker: Bruker,
        val dokumenter: List<Dokument?> = mutableListOf(),
        val tilleggsopplysninger: List<Tilleggsopplysning> = mutableListOf()) {

    data class Tilleggsopplysning(val nokkel: String, val verdi: String)

    data class Dokument private constructor(val tittel: String?, val brevkode: String? = null, val dokumentVarianter: List<DokumentVariant?>) {
        constructor(type: SkjemaType = STANDARD, dokumentVarianter: List<DokumentVariant?>) : this(type.tittel,type.kode, dokumentVarianter)
        constructor(tittel: String? = null,dokumentVariant: DokumentVariant) : this(tittel,null,listOf(dokumentVariant))

        data class DokumentVariant private constructor(val filtype: String, val fysiskDokument: String, val variantformat: String) {
            constructor(filtype: Filtype = PDFA, fysiskDokument: String, variantformat: VariantFormat = ARKIV) :this(filtype.name,fysiskDokument, variantformat.name)
            override fun toString() = "${javaClass.simpleName} [filtype=$filtype,variantformat=$variantformat,fysiskDokument=${fysiskDokument.length} bytes]"

            enum class VariantFormat {
                ORIGINAL,
                ARKIV,
                FULLVERSJON
            }

            enum class Filtype(val contentType: String) {
                PDFA(APPLICATION_PDF_VALUE),
                JPEG(IMAGE_JPEG_VALUE),
                PNG(IMAGE_PNG_VALUE),
                JSON(APPLICATION_JSON_VALUE);
            }
        }
    }

    data class Bruker(
            val id: Fødselsnummer,
            val idType: String = ID_TYPE)

    data class AvsenderMottaker(
            val id: Fødselsnummer,
            val idType: String = ID_TYPE,
            val navn: String?)



    companion object {
        private const val INNGÅENDE = "INNGAAENDE"
        private const val KANAL ="NAV_NO"
        private const val ID_TYPE = "FNR"
    }
}