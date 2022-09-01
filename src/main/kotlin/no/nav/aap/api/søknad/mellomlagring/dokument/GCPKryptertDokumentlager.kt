package no.nav.aap.api.søknad.mellomlagring.dokument

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.CONTENT_DISPOSITION
import com.google.cloud.storage.Storage.BlobField.CONTENT_TYPE
import com.google.cloud.storage.Storage.BlobField.SIZE
import com.google.cloud.storage.Storage.BlobField.TIME_CREATED
import com.google.cloud.storage.Storage.BlobGetOption.fields
import com.google.cloud.storage.Storage.BlobTargetOption.kmsKeyName
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.mellomlagring.BucketConfig
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.DokumentException.Substatus.UNSUPPORTED
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
import java.util.*

@ConditionalOnGCP
@Primary
class GCPKryptertDokumentlager(private val cfg: BucketConfig,
                               private val lager: Storage,
                               private val ctx: AuthContext,
                               private val sjekkere: List<DokumentSjekker>) : Dokumentlager {

    private val log = getLogger(javaClass)

    override fun lagreDokument(dokument: DokumentInfo) = lagreDokument(dokument, ctx.getFnr())

    fun lagreDokument(dokument: DokumentInfo, fnr: Fødselsnummer) =
        callIdAsUUID().apply {
            with(dokument) {
                val navn = navn(fnr, this@apply)
                log.trace("Lagrer $this")
                sjekkere.forEach { it.sjekk(this) }
                lager.create(newBuilder(cfg.vedlegg.navn, navn)
                    .setContentType(contentType)
                    .setContentDisposition("$contentDisposition")
                    .build(), bytes, kmsKeyName("${cfg.key}")).also {
                    log.trace(CONFIDENTIAL, "Lagret $dokument som ${it.name} i bøtte ${it.bucket}")
                }
            }
        }

    override fun lesDokument(uuid: UUID) = lesDokument(uuid, ctx.getFnr())

    override fun lesDokument(uuid: UUID, fnr: Fødselsnummer) =
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
    override fun slettDokumenter(vararg uuids: UUID) = slettUUIDs(uuids.asList(), ctx.getFnr())

    fun slettDokument(uuid: UUID, fnr: Fødselsnummer) =
        with(cfg.vedlegg) {
            with(navn(fnr, uuid)) {
                lager.delete(navn, this)
                    .also {
                        log.trace(CONFIDENTIAL, "Slettet dokument $this@with fra bøtte $navn")
                    }
            }
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
            slettUUIDs(it.deler, fnr)
        }

    fun slettUUIDs(uuids: List<UUID?>?, fnr: Fødselsnummer) =
        uuids?.forEach {
            slettDokumenter(it, fnr)
        }

    private fun slettDokumenter(uuid: UUID?, fnr: Fødselsnummer) =
        uuid?.let { id ->
            slettDokument(id, fnr)
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

        class ContentTypeException(msg: String) : DokumentException(UNSUPPORTED, msg = msg)
    }
}