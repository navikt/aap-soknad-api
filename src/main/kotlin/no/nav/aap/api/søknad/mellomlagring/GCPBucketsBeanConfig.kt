package no.nav.aap.api.søknad.mellomlagring

import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.storage.StorageOptions
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.KmsEnvelopeAeadKeyManager
import com.google.crypto.tink.integration.gcpkms.GcpKmsClient
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.threeten.bp.Duration.ofMillis
import java.util.*

@Configuration
@ConditionalOnGCP
class GCPBucketsBeanConfig(val cfg: GCPBucketConfig) {

    init {
        AeadConfig.register();
        GcpKmsClient.register(Optional.of(cfg.kekuri), Optional.empty());
    }

    @Bean
    fun aead() = KeysetHandle.generateNew(KmsEnvelopeAeadKeyManager.createKeyTemplate(cfg.kekuri,
            KeyTemplates.get("AES128_GCM"))).getPrimitive(Aead::class.java)

    @Bean
    fun retrySettings(@Value("\${mellomlagring.timeout:3000}") timeoutMs: Long) =
        RetrySettings.newBuilder()
            .setTotalTimeout(ofMillis(timeoutMs))
            .build()

    @Bean
    fun storage(retrySettings: RetrySettings) = StorageOptions
        .newBuilder()
        .setRetrySettings(retrySettings)
        .build()
        .service

}