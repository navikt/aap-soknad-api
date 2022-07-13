package no.nav.aap.api.config

import com.google.cloud.kms.v1.CryptoKey
import com.google.cloud.kms.v1.CryptoKey.CryptoKeyPurpose.ENCRYPT_DECRYPT
import com.google.cloud.kms.v1.CryptoKeyName
import com.google.cloud.kms.v1.CryptoKeyVersion.CryptoKeyVersionAlgorithm.GOOGLE_SYMMETRIC_ENCRYPTION
import com.google.cloud.kms.v1.CryptoKeyVersionTemplate
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.KeyRing
import com.google.cloud.kms.v1.KeyRingName
import com.google.cloud.kms.v1.LocationName
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.Companion.REGION
import no.nav.aap.util.LoggerUtil
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

@Component
class EncryptionIAC(private val cfgs: BucketsConfig) : InitializingBean {

    private val log = LoggerUtil.getLogger(javaClass)
    override fun afterPropertiesSet() {
        if (harRing()) {
            log.info("KeyRing ${ringNavn()} finnes allerede")
        }
        else {
            lagRing()
        }
        if (harNøkkel()) {
            log.info("CryptoKey ${nøkkelNavn()} finnes allerede")
        }
        else {
            lagNøkkel()
        }
    }

    private final fun harRing() =
        KeyManagementServiceClient.create().use { client ->
            client.listKeyRings(LocationName.of(cfgs.id, REGION)).iterateAll()
                .map { it.name }
                .contains("${ringNavn()}")
        }

    private fun ringNavn() =
        with(cfgs) {
            KeyRingName.of(id, LocationName.of(id, REGION).location, kms.ring)
        }

    private fun nøkkelNavn() =
        with(cfgs) {
            CryptoKeyName.of(id, LocationName.of(id, REGION).location, kms.ring, kms.nøkkel)
        }

    private final fun harNøkkel() =
        KeyManagementServiceClient.create().use { client ->
            client.listCryptoKeys(ringNavn()).iterateAll()
                .map { it.name }
                .contains("${nøkkelNavn()}")
        }

    fun lagRing(): KeyRing =
        with(cfgs) {
            KeyManagementServiceClient.create().use { client ->
                client.createKeyRing(LocationName.of(id, REGION), kms.ring, KeyRing.newBuilder().build()).also {
                    log.info("Lagd keyring ${it.name}")
                }
            }
        }

    fun lagNøkkel() {
        KeyManagementServiceClient.create().use { client ->
            with(cfgs) {
                client.createCryptoKey(ringNavn(), kms.nøkkel, CryptoKey.newBuilder()
                    .setPurpose(ENCRYPT_DECRYPT)
                    .setVersionTemplate(CryptoKeyVersionTemplate.newBuilder()
                        .setAlgorithm(GOOGLE_SYMMETRIC_ENCRYPTION))
                    .build()).also {
                    log.info("Lagd nøkkel $it")
                }
            }
        }
    }
}