package no.nav.aap.api.søknad.mellomlagring.dokument

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker.Companion.TIKA
import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.StandardSøknad
import org.apache.tika.Tika
import org.springframework.http.ContentDisposition
import org.springframework.http.ContentDisposition.attachment
import java.util.*

interface Dokumentlager {
    fun lesDokument(uuid: UUID): DokumentInfo?
    fun slettDokumenter(vararg uuids: UUID): Unit?
    fun slettDokumenter(søknad: StandardSøknad)
    fun lagreDokument(dokument: DokumentInfo): UUID
    fun navn(fnr: Fødselsnummer, uuid: UUID) = "${fnr.fnr}/$uuid"
    fun slettDokumenter(e: StandardEttersending) =
        e.ettersendteVedlegg.forEach { es ->
            es.ettersending.deler?.forEach { uuid ->
                uuid?.let {
                    slettDokumenter(it)
                }
            }
        }
}

data class DokumentInfo(val bytes: ByteArray,
                        val contentType: String? = TIKA.detect(bytes),
                        val contentDisposition: ContentDisposition?,
                        val createTime: Long = 0,
                        val size: Long) {
    constructor(bytes: ByteArray,
                navn: String?,
                contentType: String? = TIKA.detect(bytes),
                size: Long = bytes.size.toLong()) : this(bytes,
            contentType, navn?.let { attachment().filename(it).build() }, size = size)

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