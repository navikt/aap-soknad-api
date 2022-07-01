package no.nav.aap.api.søknad.mellomlagring.dokument

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.model.StandardSøknad
import org.apache.tika.Tika
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import java.util.*
import java.util.Objects.hash

interface Dokumentlager {
    fun lesDokument(fnr: Fødselsnummer, uuid: UUID): DokumentInfo?
    fun slettDokument(fnr: Fødselsnummer, uuid: UUID): Boolean
    fun slettDokumenter(fnr: Fødselsnummer, søknad: StandardSøknad)
    fun lagreDokument(fnr: Fødselsnummer, dokument: DokumentInfo): UUID
    fun key(fnr: Fødselsnummer, uuid: UUID) = "${hash(fnr, uuid)}"

    companion object {
        const val FILNAVN = "filnavn"
        const val FNR = "fnr"
    }

}

interface DokumentSjekker {
    fun sjekk(dokument: DokumentInfo)
}

data class DokumentInfo(val bytes: ByteArray,
                        val contentType: String?,
                        val filnavn: String?,
                        val createTime: Long = 0) {
    init {
        TIKA.detect(bytes).apply {
            if (!this.equals(contentType)) {
                throw ContentTypeException(this, "Foventet $contentType men fikk $this for $filnavn")
            }
        }
        if (contentType !in types) {
            throw ContentTypeException(msg = "Filtype $contentType er ikke støttet, må være en av $types")
        }
    }

    class ContentTypeException(val type: String? = null, msg: String) : RuntimeException(msg)

    companion object {
        private val types = listOf(APPLICATION_PDF_VALUE, IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE)
        private val TIKA = Tika()
    }

    override fun toString() =
        "${javaClass.simpleName} [filnavn=$filnavn,contentType=$contentType,createTime=$createTime,størrelse=${bytes.size} bytes]"
}