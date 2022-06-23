package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.crypto.tink.Aead
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.nio.charset.StandardCharsets.UTF_8

@ConditionalOnGCP
internal class GCPKryptertMellomlager(private val config: GCPBucketConfig,
                                      private val lager: Storage,
                                      private val aead: Aead) : Mellomlager {
    val log = getLogger(javaClass)

    override fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String) =
        lager.create(newBuilder(of(config.mellomlagring, key(fnr, type)))
            .setContentType(APPLICATION_JSON_VALUE).build(),
                aead.encrypt(value.toByteArray(UTF_8), fnr.fnr.toByteArray(UTF_8)))
            .blobId.toGsUtilUri()
            .also { log.trace(CONFIDENTIAL, "Lagret $value kryptert for $fnr") }

    override fun les(fnr: Fødselsnummer, type: SkjemaType) =
        lager.get(config.mellomlagring, key(fnr, type))?.let {
            String(aead.decrypt(it.getContent(), fnr.fnr.toByteArray(UTF_8))).also {
                log.trace(CONFIDENTIAL, "Lest $it for $fnr")
            }
        }

    override fun slett(fnr: Fødselsnummer, type: SkjemaType) =
        lager.delete(of(config.mellomlagring, key(fnr, type)))
}