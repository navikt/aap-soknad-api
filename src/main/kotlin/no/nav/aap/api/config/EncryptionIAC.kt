package no.nav.aap.api.config

import com.google.cloud.kms.v1.CryptoKey
import com.google.cloud.kms.v1.CryptoKey.CryptoKeyPurpose.ENCRYPT_DECRYPT
import com.google.cloud.kms.v1.CryptoKeyVersion.CryptoKeyVersionAlgorithm.GOOGLE_SYMMETRIC_ENCRYPTION
import com.google.cloud.kms.v1.CryptoKeyVersionTemplate
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.KeyRing
import com.google.cloud.storage.Storage
import com.google.iam.v1.Binding
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class EncryptionIAC(private val cfg: BucketsConfig, private val storage: Storage) {

    private val log = LoggerUtil.getLogger(javaClass)

    init {
        with(cfg) {
            if (harRing()) {
                log.info("KeyRing $ring finnes allerede")
            }
            else {
                lagRing()
            }
            if (harNøkkel()) {
                log.info("Nøkkel $key finnes allerede")
            }
            else {
                lagNøkkel()
            }
            setAksessForBucketServiceAccount(project)
        }
    }

    private final fun harRing() =
        KeyManagementServiceClient.create().use { client ->
            with(cfg) {
                client.listKeyRings(location).iterateAll()
                    .map { it.name }
                    .contains("$ring")
            }
        }

    private final fun harNøkkel() =
        KeyManagementServiceClient.create().use { client ->
            with(cfg) {
                client.listCryptoKeys(ring).iterateAll()
                    .map { it.name }
                    .contains("$key")
            }
        }

    private fun lagRing() =
        KeyManagementServiceClient.create().use { client ->
            with(cfg) {
                client.createKeyRing(location, ring.keyRing, KeyRing.newBuilder().build()).also {
                    log.info("Lagd keyring ${it.name}")
                }
            }
        }

    private fun lagNøkkel() {
        KeyManagementServiceClient.create().use { client ->
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
        KeyManagementServiceClient.create().use { client ->
            with(cfg) {
                client.setIamPolicy(key,
                        client.getIamPolicy(key).toBuilder()
                            .addBindings(Binding.newBuilder()
                                .setRole("roles/cloudkms.cryptoKeyEncrypterDecrypter")
                                .addMembers("serviceAccount:${storage.getServiceAccount(project).email}")
                                .build()).build())
                    .also { log.trace("Ny policy er ${it.bindingsList}") }
            }
        }

    }
}