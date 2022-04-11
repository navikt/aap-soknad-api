package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.SkjemaType
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import java.util.Objects.hash


@ConditionalOnGCP
class GCPVedlegg(@Value("\${mellomlagring.bucket:aap-vedlegg}") val bøttenavn: String,
                 val storage: Storage)  {

     fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String): UUID {
         val uuid = UUID.randomUUID()
         storage.create(
                newBuilder(BlobId.of(bøttenavn, key(fnr, type, uuid))).setContentType(APPLICATION_PDF_VALUE).build(),
                value.toByteArray(UTF_8))
        return uuid
    }

    fun les(fnr: Fødselsnummer, type: SkjemaType,uuid: UUID) = storage.get(bøttenavn, key(fnr, type, uuid))
        ?.getContent()
        ?.let { String(it, UTF_8)
        }

    fun slett(fnr: Fødselsnummer, type: SkjemaType, uuid: UUID) = storage.delete(BlobId.of(bøttenavn, key(fnr, type,uuid)))
    private fun key(fnr: Fødselsnummer, type: SkjemaType, uuid: UUID) = hash(type.name,fnr, uuid).toString()
}