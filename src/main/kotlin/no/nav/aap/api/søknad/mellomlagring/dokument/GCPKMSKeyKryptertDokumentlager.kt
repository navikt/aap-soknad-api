package no.nav.aap.api.søknad.mellomlagring.dokument

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
import no.nav.aap.api.søknad.mellomlagring.GCPBucketConfig
import no.nav.aap.api.søknad.mellomlagring.GCPBucketConfig.DokumentException
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
class GCPKMSKeyKryptertDokumentlager(private val cfg: GCPBucketConfig,
                                     private val lager: Storage,
                                     private val ctx: AuthContext,
                                     private val sjekkere: List<DokumentSjekker>) : Dokumentlager {

    private val log = getLogger(javaClass)

    override fun lagreDokument(dokument: DokumentInfo) = lagreDokument(ctx.getFnr(), dokument)

    fun lagreDokument(fnr: Fødselsnummer, dokument: DokumentInfo) =
        randomUUID().apply uuid@{
            with(dokument) {
                log.trace("Lagrer $filnavn kryptert med uuid $this@uuid  og contentType $contentType")
                sjekkere.forEach { it.sjekk(this) }
                lager.create(newBuilder(of(cfg.vedlegg, key(fnr, this@uuid)))
                    .setContentType(contentType)
                    .setMetadata(mapOf(FILNAVN to filnavn, FNR to fnr.fnr))
                    .build(),
                        bytes, kmsKeyName(cfg.kms).also { log.trace("Lagret $this kryptert med uuid $this@uuid") })
            }
        }

    override fun lesDokument(uuid: UUID) = lesDokument(ctx.getFnr(), uuid)

    fun lesDokument(fnr: Fødselsnummer, uuid: UUID) =
        lager.get(cfg.vedlegg, key(fnr, uuid), fields(METADATA, CONTENT_TYPE, TIME_CREATED))?.let { blob ->
            with(blob) {
                DokumentInfo(getContent(), contentType, metadata[FILNAVN], createTime)
                    .also {
                        log.trace("Lest kryptert dokument med uuid $uuid som  $it")
                    }
            }
        }

    override fun slettDokument(uuid: UUID) = slettDokument(ctx.getFnr(), uuid)

    fun slettDokument(fnr: Fødselsnummer, uuid: UUID) =
        lager.delete(of(cfg.vedlegg, key(fnr, uuid)))
            .also {
                log.trace(CONFIDENTIAL, "Slettet dokument $uuid for $fnr")
            }

    override fun finalize(søknad: StandardSøknad) = finalize(ctx.getFnr(), søknad)

    fun finalize(fnr: Fødselsnummer, søknad: StandardSøknad) {
        with(søknad) {
            slett(utbetalinger?.ekstraFraArbeidsgiver, fnr)
            slett(utbetalinger?.ekstraUtbetaling, fnr)
            slett(utbetalinger?.andreStønader, fnr)
            slett(this, fnr)
            slett(studier, fnr)
            slett(andreBarn, fnr)
        }
    }

    private fun slett(a: List<VedleggAware>?, fnr: Fødselsnummer) =
        a?.forEach { slett(it, fnr) }

    private fun slett(a: VedleggAware?, fnr: Fødselsnummer) =
        a?.vedlegg?.let {
            slettUUIDs(it.deler, fnr)
        }

    private fun slettUUIDs(uuids: List<UUID?>?, fnr: Fødselsnummer) =
        uuids?.forEach { slett(it, fnr) }

    private fun slett(uuid: UUID?, fnr: Fødselsnummer) =
        uuid?.let { id -> slettDokument(fnr, id).also { log.info("Slettet dokument $id") } }

    @Component
    class ContentTypeSjekker(private val cfg: GCPBucketConfig) : DokumentSjekker {

        override fun sjekk(dokument: DokumentInfo) =
            with(dokument) {
                if (!cfg.typer.contains(contentType)) {
                    throw DokumentException("Type $contentType for $filnavn er ikke blant ${cfg.typer}")
                }
                TIKA.detect(bytes).apply {
                    if (!this.equals(contentType)) {
                        throw ContentTypeException(this, "Foventet $contentType men fikk $this for $filnavn")
                    }
                }
                Unit
            }

        class ContentTypeException(val type: String? = null, msg: String) : RuntimeException(msg)
        companion object {
            private val TIKA = Tika()
        }

    }
}