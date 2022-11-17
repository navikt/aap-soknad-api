package no.nav.aap.api.søknad.mellomlagring.dokument

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.CONTENT_DISPOSITION
import com.google.cloud.storage.Storage.BlobField.CONTENT_TYPE
import com.google.cloud.storage.Storage.BlobField.SIZE
import com.google.cloud.storage.Storage.BlobField.TIME_CREATED
import com.google.cloud.storage.Storage.BlobGetOption.fields
import com.google.cloud.storage.Storage.BlobListOption.currentDirectory
import com.google.cloud.storage.Storage.BlobListOption.prefix
import com.google.cloud.storage.Storage.BlobTargetOption.kmsKeyName
import io.micrometer.core.annotation.Timed
import java.util.*
import no.nav.aap.api.error.Substatus.UNSUPPORTED
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.mellomlagring.BucketConfig
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.StørelseSjekker
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker.Companion.TIKA
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.VedleggAware
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.callIdAsUUID
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.context.annotation.Primary
import org.springframework.http.ContentDisposition.parse
import org.springframework.stereotype.Component

@ConditionalOnGCP
@Primary
@Timed
class GCPKryptertDokumentlager(private val cfg: BucketConfig,
                               private val lager: Storage,
                               private val ctx: AuthContext,
                               private val størrelseSjekker: StørelseSjekker,
                               private val sjekkere: List<DokumentSjekker>): Dokumentlager {



    private val log = getLogger(javaClass)

    override fun lagreDokument(dokument: DokumentInfo) = lagreDokument(dokument, ctx.getFnr())



    fun lagreDokument(dokument: DokumentInfo, fnr: Fødselsnummer) =
        callIdAsUUID().apply {
            with(dokument) {
                størrelseSjekker.sjekkStørrelse(cfg.vedlegg,fnr,dokument)
                val navn = navn(fnr, this@apply)
                sjekkere.forEach { it.sjekk(this) }
                log.trace("Lagrer $this")
                lager.create(newBuilder(cfg.vedlegg.navn, navn)
                    .setContentType(contentType)
                    .setContentDisposition("$contentDisposition")
                    .build(), bytes, kmsKeyName("${cfg.key}")).also {
                    log.trace("Lagret $dokument som ${this@apply} i bøtte ${it.bucket} for $fnr")
                }
            }
        }

    override fun lesDokument(uuid: UUID) = lesDokument(uuid, ctx.getFnr())

    private fun lesDokument(uuid: UUID, fnr: Fødselsnummer) =
        lager.get(cfg.vedlegg.navn, navn(fnr, uuid), fields(CONTENT_TYPE, CONTENT_DISPOSITION, TIME_CREATED, SIZE))
            ?.let { blob ->
                with(blob) {
                    DokumentInfo(getContent(), contentType, contentDisposition(), createTime, size)
                        .also {
                            log.trace(CONFIDENTIAL, "Lest $it fra ${blob.name} fra bøtte ${blob.bucket}")
                        }
                }
            } ?: run {
            log.warn("Kunne ikke lese dokument med id $uuid fra dokumentlager")
            null
        }

    private fun Blob.contentDisposition() = parse(contentDisposition)


    override fun slettAlleDokumenter() = slettAlleDokumenter(ctx.getFnr())

    override fun slettAlleDokumenter(fnr: Fødselsnummer) =
        lager.list(cfg.vedlegg.navn, prefix("${fnr.fnr}/"), currentDirectory()).iterateAll()
            .forEach {
                blob -> blob.delete().also { log.trace("Slettet ${blob.name} med status $it") }
            }

    override fun slettDokumenter(søknad: StandardSøknad) =
        slettDokumenter(søknad, ctx.getFnr())

    fun slettDokumenter(søknad: StandardSøknad, fnr: Fødselsnummer) {
        with(søknad) {
            with(utbetalinger) {
                slettDokumenter(this?.ekstraFraArbeidsgiver, fnr)
                slettDokumenter(this?.andreStønader, fnr)
            }
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
            slettDokumenter(it.deler, fnr)
        }
    override fun slettDokumenter(uuids: List<UUID>)  =
        slettDokumenter(uuids,ctx.getFnr())

    fun slettDokumenter(uuids: List<UUID?>?, fnr: Fødselsnummer) =
        uuids?.forEach {
            slettDokument(it, fnr)
        } ?: Unit

    private fun slettDokument(uuid: UUID?, fnr: Fødselsnummer) =
        uuid?.let { id ->
            slettDokument(id, fnr)
        } ?: Unit

    fun slettDokument(uuid: UUID, fnr: Fødselsnummer) =
        with(cfg.vedlegg) {
            with(navn(fnr, uuid)) {
                lager.delete(navn, this).also { log.trace("Slettet dokument $uuid fra bøtte $navn med status $it") }
            }
        }

    @Component
    class ContentTypeDokumentSjekker(private val cfg: BucketConfig) : DokumentSjekker {

        override fun sjekk(dokument: DokumentInfo) =
            with(dokument) {
                if (contentType !in cfg.vedlegg.typer) {
                    throw ContentTypeException("$contentType for $filnavn er ikke blant ${cfg.vedlegg.typer}")
                }
                TIKA.detect(bytes).run {
                    if (!equals(contentType)) {
                        throw ContentTypeException("Foventet $contentType for $filnavn, men fikk $this")
                    }
                }
            }

        class ContentTypeException(msg: String) : DokumentException(msg = msg, substatus = UNSUPPORTED)
    }
}