package no.nav.aap.api.søknad.mellomlagring.dokument

import java.util.UUID
import org.apache.tika.Tika
import org.springframework.http.ContentDisposition
import org.springframework.http.ContentDisposition.attachment
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker.Companion.TIKA
import no.nav.aap.api.søknad.fordeling.Ettersending
import no.nav.aap.api.søknad.fordeling.AAPSøknad

interface Dokumentlager {
    fun lesDokument(uuid: UUID): DokumentInfo?
    fun slettDokumenter(uuids: List<UUID>)
    fun slettDokumenter(søknad: AAPSøknad)
    fun lagreDokument(dokument: DokumentInfo): UUID
    fun navn(fnr: Fødselsnummer, uuid: UUID) = "${fnr.fnr}/$uuid"
    fun slettDokumenter(e: Ettersending) =
        e.ettersendteVedlegg.forEach { es ->
            es.ettersending.deler.forEach { uuid ->
                slettDokumenter(listOf(uuid))
            }
        }

    fun slettAlleDokumenter()

    fun slettAlleDokumenter(fnr: Fødselsnummer)
}

data class DokumentInfo(
    val bytes: ByteArray,
    val contentType: String = TIKA.detect(bytes),
    val contentDisposition: ContentDisposition?,
    val createTime: Long = 0,
    val size: Long
) {
    constructor(
        bytes: ByteArray,
        navn: String?,
        contentType: String = TIKA.detect(bytes),
        size: Long = bytes.size.toLong()
    ) : this(bytes,
        contentType, navn?.let { attachment().filename(it).build() }, size = size
    )

    val filnavn = contentDisposition?.filename

    override fun toString() =
        "${javaClass.simpleName} [filnavn=$filnavn,contentDisposition=$contentDisposition,contentType=$contentType,createTime=$createTime,størrelse=$size bytes]"
}

interface DokumentSjekker {
    fun sjekk(dokument: DokumentInfo)

    companion object {
        val TIKA = Tika()
    }
}