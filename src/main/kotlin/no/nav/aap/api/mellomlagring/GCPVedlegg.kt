package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.CONTENT_TYPE
import com.google.cloud.storage.Storage.BlobField.METADATA
import com.google.cloud.storage.Storage.BlobGetOption.fields
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.mellomlagring.Vedlegg.Companion.FILNAVN
import no.nav.aap.api.mellomlagring.Vedlegg.Companion.FNR
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import java.util.UUID.randomUUID


@ConditionalOnGCP
class GCPVedlegg(@Value("\${mellomlagring.bucket:aap-vedlegg}") private val bøtte: String, private val storage: Storage)  : Vedlegg {

    val log = LoggerUtil.getLogger(javaClass)

    override fun lagreVedlegg(fnr: Fødselsnummer, vedlegg: MultipartFile) =
         with(vedlegg) {
             randomUUID().also {
                 storage.create(
                         newBuilder(of(bøtte, key(fnr, it)))
                             .setContentType(contentType)
                             .setMetadata(mapOf(FILNAVN to originalFilename, FNR to fnr.fnr))
                             .build(), bytes)
                     .also { blob -> log.trace("Lagret vedlegg som ${blob.blobId.toGsUtilUri()}") } }
         }

    override fun lesVedlegg(fnr: Fødselsnummer, uuid: UUID) = storage.get(bøtte, key(fnr, uuid), fields(METADATA, CONTENT_TYPE))
    override fun slettVedlegg(fnr: Fødselsnummer, uuid: UUID) = storage.delete(of(bøtte, key(fnr, uuid)))


}