package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.SkjemaType
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Value
import java.util.*
import java.util.Objects.hash


@ConditionalOnGCP
class GCPVedlegg(@Value("\${mellomlagring.bucket:aap-vedlegg}") val bøttenavn: String,
                 val storage: Storage)  {

    val log = LoggerUtil.getLogger(javaClass)
     fun lagre(fnr: Fødselsnummer, contentType: String?,bytes: ByteArray): UUID {
         log.info("Lagrer vedlegg")
         val uuid = UUID.randomUUID()
         storage.create(
                 newBuilder(BlobId.of(bøttenavn, key(fnr, uuid)))
                    .setContentType(contentType)
                    .build(), bytes)
        return uuid
    }

    fun les(fnr: Fødselsnummer, uuid: UUID) =  storage.get(bøttenavn, key(fnr, uuid))?.getContent()

    fun slett(fnr: Fødselsnummer,uuid: UUID) = storage.delete(BlobId.of(bøttenavn, key(fnr,uuid)))
    private fun key(fnr: Fødselsnummer,  uuid: UUID) = hash(fnr, uuid).toString()
}