package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.CONTENT_TYPE
import com.google.cloud.storage.Storage.BlobField.METADATA
import com.google.cloud.storage.Storage.BlobGetOption.fields
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.mellomlagring.Dokumentlager.Companion.FILNAVN
import no.nav.aap.api.mellomlagring.Dokumentlager.Companion.FNR
import no.nav.aap.api.mellomlagring.virus.AttachmentException
import no.nav.aap.api.mellomlagring.virus.VirusScanner
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import java.util.*
import java.util.UUID.randomUUID


@ConditionalOnGCP
internal class GCPDokumentlager(@Value("\${mellomlagring.bucket:aap-vedlegg}") private val bøtte: String,
                                private val storage: Storage, private val scanner: VirusScanner) : Dokumentlager {

    val lovligeTyper = setOf(APPLICATION_JSON_VALUE,IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE)
    val log = LoggerUtil.getLogger(javaClass)
    override fun lagreDokument(fnr: Fødselsnummer, bytes: ByteArray, contentType: String?, originalFilename: String?) =
       randomUUID().apply {
           sjekkType(bytes, contentType)
           scanner.scan(bytes,originalFilename)
           storage.create(newBuilder(of(bøtte, key(fnr, this)))
               .setContentType(contentType)
               .setMetadata(mapOf(FILNAVN to originalFilename, FNR to fnr.fnr))
               .build(), bytes)
               .also { log.trace("Lagret $originalFilename med uuid $this") }
       }

    private fun sjekkType(bytes: ByteArray, contentType: String?) {
        with(Tika().detect(bytes)) {
            if (this != contentType) {
                throw AttachmentException("Type $this matcher ikke oppgitt $contentType")
            }
        }
        if(!lovligeTyper.contains(contentType)) {
            throw AttachmentException("Type $contentType er ikke blant $lovligeTyper")
        }
    }

    override fun lesDokument(fnr: Fødselsnummer, uuid: UUID) =
        storage.get(bøtte, key(fnr, uuid), fields(METADATA, CONTENT_TYPE))
    override fun slettDokument(fnr: Fødselsnummer, uuid: UUID) =
        storage.delete(of(bøtte, key(fnr, uuid)))
}