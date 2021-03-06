package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobTargetOption.kmsKeyName
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.mellomlagring.MellomlagringEventSubscriber.Companion.SKJEMATYPE
import no.nav.aap.api.søknad.mellomlagring.MellomlagringEventSubscriber.Companion.UUID_
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager.Companion.FNR
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.callId
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.nio.charset.StandardCharsets.UTF_8

@ConditionalOnGCP
internal class GCPKryptertMellomlager(private val cfg: BucketsConfig,
                                      private val lager: Storage,
                                      private val ctx: AuthContext) : Mellomlager {
    val log = getLogger(javaClass)

    override fun lagre(type: SkjemaType, value: String) = lagre(ctx.getFnr(), type, value)

    fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String) =
        with(cfg) {
            lager.create(newBuilder(of(mellom.navn, navn(fnr, type)))
                .setMetadata(mapOf(SKJEMATYPE to type.name, FNR to fnr.fnr, UUID_ to callId()))
                .setContentType(APPLICATION_JSON_VALUE).build(), value.toByteArray(UTF_8), kmsKeyName("$key"))
                .blobId.toGsUtilUri()
                .also {
                    log.trace(CONFIDENTIAL, "Lagret kryptert $value for $fnr som $it")
                }
        }

    override fun les(type: SkjemaType) = les(ctx.getFnr(), type)

    fun les(fnr: Fødselsnummer, type: SkjemaType) =
        lager.get(cfg.mellom.navn, navn(fnr, type))?.let { blob ->
            String(blob.getContent()).also {
                log.trace(CONFIDENTIAL, "Lest kryptert verdi $it for $fnr")
            }
        }

    override fun slett(type: SkjemaType) = slett(ctx.getFnr(), type)

    fun slett(fnr: Fødselsnummer, type: SkjemaType) =
        lager.delete(of(cfg.mellom.navn, navn(fnr, type)).also {
            log.trace(CONFIDENTIAL, "Slettet ${it.name} for $fnr")
        })
}