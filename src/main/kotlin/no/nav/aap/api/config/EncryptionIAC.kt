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
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

@Component
class EncryptionIAC(private val cfgs: BucketsConfig, private val storage: Storage) : InitializingBean {

    private val log = LoggerUtil.getLogger(javaClass)
    override fun afterPropertiesSet() =
        with(cfgs) {
            if (harRing()) {
                log.info("KeyRing $ring finnes allerede")
            }
            else {
                lagRing()
            }
            if (harNøkkel()) {
                log.info("Nøkkel $nøkkel finnes allerede")
            }
            else {
                lagNøkkel()
            }
            setAksessForBucketServiceAccount()
        }

    fun listRinger() =
        KeyManagementServiceClient.create().use { client ->
            client.listKeyRings(cfgs.location).iterateAll()
        }

    private final fun harRing() =
        listRinger()
            .map { it.name }
            .contains(cfgs.ringNavn)

    final fun listNøkler() =
        KeyManagementServiceClient.create().use { client ->
            client.listCryptoKeys(cfgs.ring).iterateAll()
        }

    private final fun harNøkkel() =
        listNøkler()
            .map { it.name }
            .contains(cfgs.nøkkelNavn)

    fun lagRing(): KeyRing =
        with(cfgs) {
            KeyManagementServiceClient.create().use { client ->
                client.createKeyRing(location, ring.keyRing, KeyRing.newBuilder().build()).also {
                    log.info("Lagd keyring ${it.name}")
                }
            }
        }

    fun lagNøkkel() {
        with(cfgs) {
            KeyManagementServiceClient.create().use { client ->
                client.createCryptoKey(ring, nøkkel.cryptoKey, CryptoKey.newBuilder()
                    .setPurpose(ENCRYPT_DECRYPT)
                    .setVersionTemplate(CryptoKeyVersionTemplate.newBuilder()
                        .setAlgorithm(GOOGLE_SYMMETRIC_ENCRYPTION))
                    .build()).also {
                    log.info("Lagd nøkkel $it")
                }
            }
        }
    }

    fun setAksessForBucketServiceAccount() {
        with(cfgs) {
            KeyManagementServiceClient.create().use { client ->
                client.setIamPolicy(nøkkel,
                        client.getIamPolicy(nøkkel).toBuilder().addBindings(Binding.newBuilder()
                            .setRole("roles/cloudkms.cryptoKeyEncrypterDecrypter")
                            .addMembers("serviceAccount:${storage.getServiceAccount(project).email}")
                            .build()).build())
                    .also { log.trace("Ny policy er ${it.bindingsList}") }
            }
        }

    }
}