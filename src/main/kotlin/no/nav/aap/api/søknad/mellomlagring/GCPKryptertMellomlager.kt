package no.nav.aap.api.søknad.mellomlagring

import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobTargetOption.kmsKeyName
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.SKJEMATYPE
import no.nav.aap.api.søknad.mellomlagring.BucketConfig.Companion.UUID_
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.callId
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.nio.charset.StandardCharsets.UTF_8

@ConditionalOnGCP
internal class GCPKryptertMellomlager(private val cfg: BucketConfig,
                                      private val lager: Storage,
                                      private val ctx: AuthContext) : Mellomlager {
    val log = getLogger(javaClass)

    override fun lagre(value: String, type: SkjemaType) = lagre(value, type, ctx.getFnr())

    fun lagre(value: String, type: SkjemaType, fnr: Fødselsnummer) =
        with(cfg) {
            lager.create(newBuilder(mellom.navn, navn(fnr, type))
                .setMetadata(mapOf(SKJEMATYPE to type.name, UUID_ to callId()))
                .setContentType(APPLICATION_JSON_VALUE).build(), value.toByteArray(UTF_8), kmsKeyName("$key"))
                .also {
                    log.trace(CONFIDENTIAL, "Lagret mellomlagret $value som ${it.name} i bøtte ${mellom.navn}")
                }
        }.name

    override fun les(type: SkjemaType) = les(type, ctx.getFnr())

    fun les(type: SkjemaType, fnr: Fødselsnummer) =
        with(cfg.mellom) {
            with(navn(fnr, type)) {
                lager.get(navn, this)?.let { blob ->
                    String(blob.getContent()).also {
                        log.trace(CONFIDENTIAL, "Lest mellomlagret verdi $it fra $this og bøtte $navn")
                    }
                }
            }
        }

    override fun slett(type: SkjemaType) = slett(type, ctx.getFnr())

    fun slett(type: SkjemaType, fnr: Fødselsnummer) =
        with(cfg.mellom) {
            with(navn(fnr, type)) {
                lager.delete(navn, this).also {
                    log.trace(CONFIDENTIAL, "Slettet mellomlagret $this fra bøtte $navn")
                }
            }
        }
}