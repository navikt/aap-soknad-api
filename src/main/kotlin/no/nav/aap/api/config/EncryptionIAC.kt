package no.nav.aap.api.config

import com.google.cloud.kms.v1.CryptoKey
import com.google.cloud.kms.v1.CryptoKey.CryptoKeyPurpose.ENCRYPT_DECRYPT
import com.google.cloud.kms.v1.CryptoKeyVersion.CryptoKeyVersionAlgorithm.GOOGLE_SYMMETRIC_ENCRYPTION
import com.google.cloud.kms.v1.CryptoKeyVersionTemplate
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.KeyRing
import com.google.cloud.storage.Storage
import com.google.iam.v1.Binding
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import no.nav.aap.api.søknad.mellomlagring.BucketConfig
import no.nav.aap.util.LoggerUtil

@Component
class EncryptionIAC(private val cfg: BucketConfig, private val storage: Storage) : CommandLineRunner {

    override fun run(vararg args: String?) {
        with(cfg) {
            log.trace("IAC encryption init")
            if (harRing()) {
                log.info("KeyRing $ring finnes allerede")
            }
            else {
                lagRing()
            }
            if (harKey()) {
                log.info("Nøkkel $key finnes allerede")
            }
            else {
                lagKey()
            }
            setAksessForBucketServiceAccount(project)
        }
    }
    private final fun harRing() =
        KMS.use { client ->
            with(cfg) {
                client.listKeyRings(location).iterateAll()
                    .any { it.name == "$ring" }
            }
        }

    private final fun harKey() =
        KMS.use { client ->
            with(cfg) {
                client.listCryptoKeys(ring).iterateAll()
                    .any { it.name == "$key" }
            }
        }

    private fun lagRing() =
        KMS.use { client ->
            with(cfg) {
                client.createKeyRing(location, ring.keyRing, KeyRing.newBuilder().build()).also {
                    log.info("Lagd keyring ${it.name}")
                }
            }
        }

    private fun lagKey() {
       KMS.use { client ->
            with(cfg) {
                client.createCryptoKey(ring,
                        key.cryptoKey,
                        CryptoKey.newBuilder()
                            .setPurpose(ENCRYPT_DECRYPT)
                            .setVersionTemplate(CryptoKeyVersionTemplate.newBuilder()
                                .setAlgorithm(GOOGLE_SYMMETRIC_ENCRYPTION))
                            .build()).also {
                    log.info("Lagd nøkkel $it")
                }
            }
        }
    }

    private fun setAksessForBucketServiceAccount(project: String) {
        KMS.use { client ->
            with(cfg) {
                client.setIamPolicy(key,
                        client.getIamPolicy(key).toBuilder()
                            .addBindings(Binding.newBuilder()
                                .setRole(ENCRYPT_DECRYPT_ROLE)
                                .addMembers("serviceAccount:${storage.getServiceAccount(project).email}")
                                .build()).build())
                    .also {
                        log.trace("Ny policy er {}", it.bindingsList)
                    }
            }
        }
    }

    companion object {
        private val KMS  = KeyManagementServiceClient.create()
        private const val ENCRYPT_DECRYPT_ROLE = "roles/cloudkms.cryptoKeyEncrypterDecrypter"
        private val log = LoggerUtil.getLogger(EncryptionIAC::class.java)
    }


}