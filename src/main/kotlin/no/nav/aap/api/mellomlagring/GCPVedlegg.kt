package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.CONTENT_TYPE
import com.google.cloud.storage.Storage.BlobField.METADATA
import com.google.cloud.storage.Storage.BlobGetOption.fields
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.util.Objects.hash


@ConditionalOnGCP
class GCPVedlegg(@Value("\${mellomlagring.bucket:aap-vedlegg}") private val bøttenavn: String, private val storage: Storage)  {

     fun lagre(fnr: Fødselsnummer, vedlegg: MultipartFile): UUID {
         val uuid = UUID.randomUUID()
         storage.create(
                 newBuilder(BlobId.of(bøttenavn, "${hash(fnr, uuid)}"))
                     .setContentType(vedlegg.contentType)
                     .setMetadata(mapOf(FILNAVN to vedlegg.originalFilename))
                     .build(), vedlegg.bytes)
        return uuid
    }

    fun les(fnr: Fødselsnummer, uuid: UUID) = storage.get(bøttenavn, "${hash(fnr, uuid)}", fields(METADATA, CONTENT_TYPE))

    fun slett(fnr: Fødselsnummer,uuid: UUID) = storage.delete(BlobId.of(bøttenavn, "${hash(fnr, uuid)}"))


    companion object {
         const val FILNAVN = "filnavn"
    }

}