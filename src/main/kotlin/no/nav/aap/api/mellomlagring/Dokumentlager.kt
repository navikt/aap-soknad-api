package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.Blob
import no.nav.aap.api.felles.Fødselsnummer
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.util.Objects.hash

interface Dokumentlager {
    fun lesDokument(fnr: Fødselsnummer, uuid: UUID): Blob?
    fun slettDokument(fnr: Fødselsnummer, uuid: UUID): Boolean
    fun lagreDokument(fnr: Fødselsnummer, vedlegg: MultipartFile) =
        lagreDokument(fnr, vedlegg.bytes, vedlegg.contentType, vedlegg.originalFilename)
    fun lagreDokument(fnr: Fødselsnummer, bytes: ByteArray, contentType: String?, originalFilename: String?): UUID
    fun key(fnr: Fødselsnummer, uuid: UUID) = "${hash(fnr, uuid)}"

    companion object {
        const val FILNAVN = "filnavn"
        const val FNR = "fnr"
    }
}