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
    override fun afterPropertiesSet() {
        if (harRing()) {
            log.info("KeyRing ${cfgs.ringNavn} finnes allerede")
        }
        else {
            lagRing()
        }
        if (harNøkkel()) {
            log.info("CryptoKey ${cfgs.nøkkelNavn} finnes allerede")
        }
        else {
            lagNøkkel()
        }
        setKeyAksessForBucketServiceAccount()
    }

    fun listRinger() =
        KeyManagementServiceClient.create().use { client ->
            client.listKeyRings(cfgs.locationNavn).iterateAll()
        }

    private final fun harRing() =
        listRinger()
            .map { it.name }
            .contains(cfgs.ringNavn.toString())

    final fun listNøkler() =
        KeyManagementServiceClient.create().use { client ->
            client.listCryptoKeys(cfgs.ringNavn).iterateAll()
        }

    private final fun harNøkkel() =
        listNøkler()
            .map { it.name }
            .contains(cfgs.nøkkelNavn)

    fun lagRing(): KeyRing =
        KeyManagementServiceClient.create().use { client ->
            client.createKeyRing(cfgs.locationNavn, cfgs.kms.ring, KeyRing.newBuilder().build()).also {
                log.info("Lagd keyring ${it.name}")
            }
        }

    fun lagNøkkel() {
        KeyManagementServiceClient.create().use { client ->
            client.createCryptoKey(cfgs.ringNavn, cfgs.kms.key, CryptoKey.newBuilder()
                .setPurpose(ENCRYPT_DECRYPT)
                .setVersionTemplate(CryptoKeyVersionTemplate.newBuilder()
                    .setAlgorithm(GOOGLE_SYMMETRIC_ENCRYPTION))
                .build()).also {
                log.info("Lagd nøkkel $it")
            }
        }
    }

    fun setKeyAksessForBucketServiceAccount() {
        KeyManagementServiceClient.create().use { client ->
            client.setIamPolicy(cfgs.nøkkel,
                    client.getIamPolicy(cfgs.nøkkel).toBuilder().addBindings(Binding.newBuilder()
                        .setRole("roles/cloudkms.cryptoKeyEncrypterDecrypter")
                        .addMembers("serviceAccount:${storage.getServiceAccount(cfgs.id).email}")
                        .build()).build())
                .also { log.trace("Ny policy er ${it.bindingsList}") }
        }
    }
}