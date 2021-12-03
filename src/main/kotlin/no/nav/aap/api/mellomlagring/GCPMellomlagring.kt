package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.SkjemaType
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.client.HttpClientErrorException.create
import java.nio.charset.Charset.defaultCharset
import java.nio.charset.StandardCharsets.UTF_8


@ConditionalOnGCP
class GCPMellomlagring(@Value("\${mellomlagring.bucket:aap-mellomlagring}") val bøttenavn: String,
                       val storage: Storage) : Mellomlagring {
    private val log = getLogger(GCPMellomlagring::class.java)

    override fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String) {
        storage.create(
                BlobInfo.newBuilder(blobFra(fnr, type))
                    .setContentType(APPLICATION_JSON_VALUE).build(), value.toByteArray(UTF_8))
    }


    override fun les(fnr: Fødselsnummer, type: SkjemaType): String? {
        return storage.get(bøttenavn, key(fnr, type))?.getContent()?.let { String(it) }
            ?: throw exception(NOT_FOUND, "Ingen mellomlagring funnet")
    }

    private fun exception(status: HttpStatus, msg: String) =
        create(status, msg, HttpHeaders(), ByteArray(0), defaultCharset())

    override fun slett(fnr: Fødselsnummer, type: SkjemaType) = storage.delete(blobFra(fnr, type))
    private fun key(fnr: Fødselsnummer, type: SkjemaType) = type.name + "_" + fnr.fnr
    private fun blobFra(fnr: Fødselsnummer, type: SkjemaType) = BlobId.of(bøttenavn, key(fnr, type))
}