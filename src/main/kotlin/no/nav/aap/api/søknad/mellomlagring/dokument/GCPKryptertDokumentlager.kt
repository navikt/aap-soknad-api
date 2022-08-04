package no.nav.aap.api.søknad.mellomlagring.dokument

import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.CONTENT_TYPE
import com.google.cloud.storage.Storage.BlobField.METADATA
import com.google.cloud.storage.Storage.BlobField.TIME_CREATED
import com.google.cloud.storage.Storage.BlobGetOption.fields
import com.google.cloud.storage.Storage.BlobTargetOption.kmsKeyName
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.mellomlagring.BucketConfig
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.FILNAVN
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.UUID_
import no.nav.aap.api.søknad.mellomlagring.DokumentException
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
class GCPKryptertDokumentlager(private val cfg: BucketConfig,
                               private val lager: Storage,
                               private val ctx: AuthContext,
                               private val sjekkere: List<DokumentSjekker>) : Dokumentlager {

    private val log = getLogger(javaClass)

    override fun lagreDokument(dokument: DokumentInfo) = lagreDokument(ctx.getFnr(), dokument)

    fun lagreDokument(fnr: Fødselsnummer, dokument: DokumentInfo) =
        randomUUID().apply {
            with(dokument) {
                log.trace("Lagrer $filnavn kryptert som ${navn(fnr, this@apply)} og contentType $contentType")
                sjekkere.forEach { it.sjekk(this) }
                lager.create(newBuilder(cfg.vedlegg.navn, navn(fnr, this@apply))
                    .setContentType(contentType)
                    .setMetadata(mapOf(FILNAVN to filnavn, UUID_ to "${this@apply}"))
                    .build(), bytes, kmsKeyName("${cfg.key}")).also {
                    log.trace(CONFIDENTIAL, "Lagret $dokument som ${it.name} i bøtte ${cfg.vedlegg.navn}")
                }
            }
        }

    override fun lesDokument(uuid: UUID) = lesDokument(ctx.getFnr(), uuid)

    fun lesDokument(fnr: Fødselsnummer, uuid: UUID) =
        lager.get(cfg.vedlegg.navn, navn(fnr, uuid), fields(METADATA, CONTENT_TYPE, TIME_CREATED))
            ?.let { blob ->
                with(blob) {
                    DokumentInfo(getContent(), contentType, metadata[FILNAVN], createTime)
                        .also {
                            log.trace(CONFIDENTIAL,
                                    "Lest dokument fra ${blob.name} (originalt navn ${it.filnavn}) fra bøtte ${cfg.vedlegg.navn}")
                        }
                }
            }

    override fun slettDokument(uuid: UUID) = slettDokument(ctx.getFnr(), uuid)

    fun slettDokument(fnr: Fødselsnummer, uuid: UUID) =
        with(navn(fnr, uuid)) {
            lager.delete(cfg.vedlegg.navn, this)
                .also {
                    log.trace(CONFIDENTIAL, "Slettet dokument $this fra bøtte ${cfg.vedlegg.navn}")
                }
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
    class ContentTypeDokumentSjekker(private val cfg: BucketConfig) : DokumentSjekker {

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