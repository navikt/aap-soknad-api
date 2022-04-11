package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.util.Objects.hash


@ConditionalOnGCP
class GCPVedlegg(@Value("\${mellomlagring.bucket:aap-vedlegg}") private val bøttenavn: String, private val storage: Storage)  {

    val log = LoggerUtil.getLogger(javaClass)
     fun lagre(fnr: Fødselsnummer, file: MultipartFile): UUID {
         log.info("Lagrer vedlegg fra ${file.originalFilename}")
         val uuid = UUID.randomUUID()
         storage.create(
                 newBuilder(BlobId.of(bøttenavn, "${hash(fnr, uuid)}"))
                    .setContentType(file.contentType)
                    .build(), file.bytes)
        return uuid
    }

    fun les(fnr: Fødselsnummer, uuid: UUID) = storage.get(bøttenavn, "${hash(fnr, uuid)}")

    fun slett(fnr: Fødselsnummer,uuid: UUID) = storage.delete(BlobId.of(bøttenavn, "${hash(fnr, uuid)}"))
}