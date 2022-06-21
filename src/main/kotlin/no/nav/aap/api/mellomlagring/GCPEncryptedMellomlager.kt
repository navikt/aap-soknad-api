package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.METADATA
import com.google.cloud.storage.Storage.BlobGetOption.fields
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates.get
import com.google.crypto.tink.KeysetHandle.generateNew
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.KmsEnvelopeAeadKeyManager.createKeyTemplate
import com.google.crypto.tink.integration.gcpkms.GcpKmsClient
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

@ConditionalOnGCP
internal class GCPEncryptedMellomlager(@Value("\${mellomlagring.bucket:aap-mellomlagring}") private val bøtte: String,
                                       @Value("\${mellomlagring.bucket.kekuri:gcp-kms://projects/aap-dev-e48b/locations/europe-north1/keyRings/aap-mellomlagring-kms/cryptoKeys/mellomlagring}") private
                                       val kekUri: String,
                                       private val lager: Storage) : Mellomlager {
    val log = LoggerUtil.getLogger(javaClass)

    init {
        AeadConfig.register();
        GcpKmsClient.register(Optional.of(kekUri), Optional.empty());
    }

    val aead = generateNew(createKeyTemplate(kekUri, get("AES128_GCM"))).getPrimitive(Aead::class.java)

    override fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String) =
        lager.create(newBuilder(of(bøtte, key(fnr, type)))
            .setContentType(APPLICATION_JSON_VALUE).build(),
                aead.encrypt(value.toByteArray(UTF_8), fnr.fnr.toByteArray(UTF_8)))
            .blobId.toGsUtilUri()
            .also { log.trace(CONFIDENTIAL, "Lagret $it for $fnr") }

    override fun les(fnr: Fødselsnummer, type: SkjemaType) =
        lager.get(bøtte, key(fnr, type), fields(METADATA))?.let {
            String(aead.decrypt(it.getContent(), fnr.fnr.toByteArray(UTF_8))).also {
                log.trace(CONFIDENTIAL, "Lest $it for $fnr")
            }
        }

    override fun slett(fnr: Fødselsnummer, type: SkjemaType) =
        lager.delete(of(bøtte, key(fnr, type)))
}