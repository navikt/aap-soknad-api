package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.arkiv.Filtype.PDFA
import no.nav.aap.api.søknad.arkiv.VariantFormat.ARKIV
import no.nav.aap.util.Constants.AAP
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import java.util.*
import java.util.Base64.getEncoder

data class Journalpost(
        val journalposttype: String = "INNGAAENDE",
        val tema: String = AAP.uppercase(),
        val eksternReferanseId: UUID,
        val kanal: String = "NAV_NO",
        val tittel: String,
        val avsenderMottaker: AvsenderMottaker,
        val bruker: Bruker,
        val dokumenter: List<Dokument?> = mutableListOf(),
        val tilleggsopplysninger: List<Tilleggsopplysning> = mutableListOf()
)

data class Tilleggsopplysning(val nokkel: String, val verdi: String)

data class Dokument private constructor(val tittel: String?, val brevkode: String? = null, val dokumentVarianter: List<DokumentVariant?>) {
    constructor(type: SkjemaType = STANDARD, dokumentVarianter: List<DokumentVariant?>) : this(type.tittel,type.kode, dokumentVarianter)
    constructor(tittel: String? = null,dokumentVariant: DokumentVariant) : this(tittel,null,listOf(dokumentVariant))

}

data class DokumentVariant private constructor(val filtype: String, val fysiskDokument: String, val variantformat: String) {
    constructor(filtype: Filtype = PDFA, fysiskDokument: String, variantformat: VariantFormat = ARKIV) :this(filtype.name,fysiskDokument, variantformat.name)
        override fun toString() = "${javaClass.simpleName} [filtype=$filtype,variantformat=$variantformat,fysiskDokument=${fysiskDokument.length} bytes]"
}

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
    companion object {
        private val map = Filtype.values().associateBy(Filtype::contentType)
        fun of(contentType: String) = map[contentType] ?: throw IllegalArgumentException("Content type $contentType er ikke støttet, lovlige verider er ${values()}}")
    }
}

private const val ID_TYPE = "FNR"

data class Bruker(
        val id: Fødselsnummer,
        val idType: String = ID_TYPE
)

data class AvsenderMottaker(
        val id: Fødselsnummer,
        val idType: String = ID_TYPE,
        val navn: String?
)

fun ByteArray.somPDFVariant() = DokumentVariant(PDFA, encode(),ARKIV)
fun ByteArray.encode() = getEncoder().encodeToString(this)