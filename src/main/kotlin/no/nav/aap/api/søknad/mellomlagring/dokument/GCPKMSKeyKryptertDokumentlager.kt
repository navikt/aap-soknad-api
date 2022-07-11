package no.nav.aap.api.søknad.mellomlagring.dokument

import com.google.cloud.kms.v1.CryptoKey
import com.google.cloud.kms.v1.CryptoKey.CryptoKeyPurpose.ENCRYPT_DECRYPT
import com.google.cloud.kms.v1.CryptoKeyVersion.CryptoKeyVersionAlgorithm.GOOGLE_SYMMETRIC_ENCRYPTION
import com.google.cloud.kms.v1.CryptoKeyVersionTemplate
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.KeyRing
import com.google.cloud.kms.v1.KeyRingName
import com.google.cloud.kms.v1.LocationName
import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.CONTENT_TYPE
import com.google.cloud.storage.Storage.BlobField.METADATA
import com.google.cloud.storage.Storage.BlobField.TIME_CREATED
import com.google.cloud.storage.Storage.BlobGetOption.fields
import com.google.cloud.storage.Storage.BlobTargetOption.kmsKeyName
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig
import no.nav.aap.api.søknad.mellomlagring.BucketsConfig.Companion.REGION
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager.Companion.FILNAVN
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager.Companion.FNR
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.VedleggAware
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.apache.tika.Tika
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.*
import java.util.UUID.randomUUID

@ConditionalOnGCP
@Primary
class GCPKMSKeyKryptertDokumentlager(private val cfg: BucketsConfig,
                                     private val lager: Storage,
                                     private val ctx: AuthContext,
                                     private val sjekkere: List<DokumentSjekker>) : Dokumentlager {

    private val log = getLogger(javaClass)

    init {
        listRings()
        lagKey()
    }

    private final fun listRings() =
        with(cfg) {
            KeyManagementServiceClient.create().use { client ->
                client.listKeyRings(LocationName.of(id, REGION)).iterateAll()
                    .forEach {
                        log.info("Lister for ring ${it.name}")
                        listKeys(it.name)
                    }
            }
        }

    private final fun listKeys(ringName: String) =
        with(cfg) {
            KeyManagementServiceClient.create().use { client ->
                client.listCryptoKeys(ringName).iterateAll()
                    .also { cryptoKeys -> log.info("name $ringName, Keys: ${cryptoKeys.forEach { log.info("key: $it") }}") }
            }
        }

    fun lagKeyRing(): KeyRing =
        with(cfg) {
            KeyManagementServiceClient.create().use { client ->
                client.createKeyRing(LocationName.of(cfg.id, REGION), kms.ring, KeyRing.newBuilder().build()).also {
                    log.info("Lagd keyring $it")
                }
            }
        }

    fun lagKey() {
        KeyManagementServiceClient.create().use { client ->
            val keyRingName = KeyRingName.of(cfg.id, LocationName.of(cfg.id, REGION).location, cfg.kms.ring)
            log.trace("Create key fra keyring $keyRingName")
            val key = CryptoKey.newBuilder()
                .setPurpose(ENCRYPT_DECRYPT)
                .setVersionTemplate(CryptoKeyVersionTemplate.newBuilder()
                    .setAlgorithm(GOOGLE_SYMMETRIC_ENCRYPTION))
                .build()
            log.trace("Lag snart key $key")

            //val createdKey = client.createCryptoKey(keyRingName, cfg.kms.key, key)
            // System.out.printf("Created symmetric key %s%n", createdKey.name)
        }
    }

    override fun lagreDokument(dokument: DokumentInfo) = lagreDokument(ctx.getFnr(), dokument)

    fun lagreDokument(fnr: Fødselsnummer, dokument: DokumentInfo) =
        randomUUID().apply {
            with(dokument) {
                log.trace("Lagrer $filnavn kryptert, med uuid $this@uuid  og contentType $contentType")
                sjekkere.forEach { it.sjekk(this) }
                lager.create(newBuilder(of(cfg.vedlegg.navn, this@apply.toString()))
                    .setContentType(contentType)
                    .setMetadata(mapOf(FILNAVN to filnavn, "uuid" to this@apply.toString(), FNR to fnr.fnr))
                    .build(), bytes, kmsKeyName(cfg.vedlegg.kms))
            }
        }.also {
            log.trace("Lagret $this kryptert med uuid $it")
        }

    override fun lesDokument(uuid: UUID) = lesDokument(ctx.getFnr(), uuid)

    fun lesDokument(fnr: Fødselsnummer, uuid: UUID) =
        lager.get(cfg.vedlegg.navn, uuid.toString(), fields(METADATA, CONTENT_TYPE, TIME_CREATED))
            ?.let { blob ->
                with(blob) {
                    DokumentInfo(getContent(), contentType, metadata[FILNAVN], createTime)
                        .also {
                            log.trace("Lest kryptert dokument med uuid $uuid som  $it")
                        }
                }
            }

    override fun slettDokument(uuid: UUID) = slettDokument(ctx.getFnr(), uuid)

    fun slettDokument(fnr: Fødselsnummer, uuid: UUID) =
        lager.delete(of(cfg.vedlegg.navn, "$uuid"))
            .also {
                log.trace(CONFIDENTIAL, "Slettet dokument $uuid for $fnr")
            }

    override fun slettDokumenter(søknad: StandardSøknad) =
        slettDokumenter(ctx.getFnr(), søknad)

    fun slettDokumenter(fnr: Fødselsnummer, søknad: StandardSøknad) {
        with(søknad) {
            with(utbetalinger) {
                slettDokumenter(this?.ekstraFraArbeidsgiver, fnr)
                slettDokumenter(this?.ekstraUtbetaling, fnr)
                slettDokumenter(this?.andreStønader, fnr)
            }
            slettDokumenter(this, fnr)
            slettDokumenter(studier, fnr)
            slettDokumenter(andreBarn, fnr)
        }
    }

    private fun slettDokumenter(a: List<VedleggAware>?, fnr: Fødselsnummer) =
        a?.forEach {
            slettDokumenter(it, fnr)
        }

    private fun slettDokumenter(a: VedleggAware?, fnr: Fødselsnummer) =
        a?.vedlegg?.let {
            slettUUIDs(it.deler, fnr)
        }

    private fun slettUUIDs(uuids: List<UUID?>?, fnr: Fødselsnummer) =
        uuids?.forEach {
            slettDokumenter(it, fnr)
        }

    private fun slettDokumenter(uuid: UUID?, fnr: Fødselsnummer) =
        uuid?.let { id ->
            slettDokument(fnr, id)
        }

    @Component
    class ContentTypeDokumentSjekker(private val cfg: BucketsConfig) : DokumentSjekker {

        override fun sjekk(dokument: DokumentInfo) =
            with(dokument) {
                if (contentType !in cfg.vedlegg.typer) {
                    throw DokumentException("Type $contentType for $filnavn er ikke blant ${cfg.vedlegg.typer}")
                }
                TIKA.detect(bytes).run {
                    if (!equals(contentType)) {
                        throw ContentTypeException(this, "Foventet $contentType for $filnavn, men fikk $this")
                    }
                }
            }

        class ContentTypeException(val type: String? = null, msg: String) : RuntimeException(msg)
        companion object {
            private val TIKA = Tika()
        }
    }
}