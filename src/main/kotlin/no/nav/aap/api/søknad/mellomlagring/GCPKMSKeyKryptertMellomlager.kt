package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobTargetOption
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.nio.charset.StandardCharsets.UTF_8

@ConditionalOnGCP
internal class GCPKMSKeyKryptertMellomlager(private val cfg: GCPBucketConfig,
                                            private val lager: Storage) : Mellomlager {
    val log = getLogger(javaClass)

    override fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String) =
        lager.create(newBuilder(of(cfg.mellomlagring, key(fnr, type)))
            .setContentType(APPLICATION_JSON_VALUE).build(), value.toByteArray(UTF_8),
                BlobTargetOption.kmsKeyName(cfg.kms))
            .blobId.toGsUtilUri()
            .also { log.trace(CONFIDENTIAL, "Lagret kryptert $value  for $fnr") }

    override fun les(fnr: Fødselsnummer, type: SkjemaType) =
        lager.get(cfg.mellomlagring, key(fnr, type))?.let {
            String(it.getContent()).also {
                log.trace(CONFIDENTIAL, "Lest kryptert $it for $fnr")
            }
        }

    override fun slett(fnr: Fødselsnummer, type: SkjemaType) =
        lager.delete(of(cfg.mellomlagring, key(fnr, type)))
}