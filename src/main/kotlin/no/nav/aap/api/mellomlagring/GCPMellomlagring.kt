package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.SkjemaType
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.nio.charset.StandardCharsets.UTF_8


@ConditionalOnGCP
class GCPMellomlagring(@Value("\${mellomlagring.bucket:aap-mellomlagring}") val bøttenavn: String,
                       val storage: Storage) : Mellomlagring {

    override fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String) {
        storage.create(
                newBuilder(blobFra(fnr, type)).setContentType(APPLICATION_JSON_VALUE).build(),
                value.toByteArray(UTF_8))
    }

    override fun les(fnr: Fødselsnummer, type: SkjemaType) =
        storage.get(bøttenavn, key(fnr, type))?.getContent()?.let { String(it) }

    override fun slett(fnr: Fødselsnummer, type: SkjemaType) = storage.delete(blobFra(fnr, type))
    private fun key(fnr: Fødselsnummer, type: SkjemaType) = type.name.plus("_").plus(fnr.fnr)
    private fun blobFra(fnr: Fødselsnummer, type: SkjemaType) = BlobId.of(bøttenavn, key(fnr, type))
}