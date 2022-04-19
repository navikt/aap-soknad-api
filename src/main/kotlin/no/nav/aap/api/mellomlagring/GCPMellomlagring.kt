package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.formidling.SkjemaType
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.nio.charset.StandardCharsets.UTF_8


@ConditionalOnGCP
class GCPMellomlagring(@Value("\${mellomlagring.bucket:aap-mellomlagring}") private val bøtte: String, private val storage: Storage) : Mellomlagring {

    val log = LoggerUtil.getLogger(javaClass)

    override fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String) =
        storage.create(
                newBuilder(of(bøtte, key(fnr, type)))
                    .setContentType(APPLICATION_JSON_VALUE).build(),
                value.toByteArray(UTF_8))
            .blobId.toGsUtilUri()
            .also { log.trace("Mellomlagret som $it") }

    override fun les(fnr: Fødselsnummer, type: SkjemaType) = storage.get(bøtte, key(fnr, type))
        ?.getContent()
        ?.let { String(it, UTF_8)
        }

    override fun slett(fnr: Fødselsnummer, type: SkjemaType) = storage.delete(of(bøtte, key(fnr, type)))
}