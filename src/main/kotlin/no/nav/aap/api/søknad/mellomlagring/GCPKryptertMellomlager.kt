package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.TIME_CREATED
import com.google.cloud.storage.Storage.BlobListOption
import com.google.cloud.storage.Storage.BlobTargetOption.kmsKeyName
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDateTime
import java.time.ZoneOffset
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

@ConditionalOnGCP
internal class GCPKryptertMellomlager(private val cfg: BucketConfig,
                                      private val lager: Storage,
                                      private val mapper: ObjectMapper,
                                      private val ctx: AuthContext) : Mellomlager {
    val log = getLogger(javaClass)

    override fun lagre(value: String, type: SkjemaType) = lagre(value, type, ctx.getFnr())

    fun lagre(value: String, type: SkjemaType, fnr: Fødselsnummer) =
        with(cfg) {
            lager.create(newBuilder(mellom.navn, navn(fnr, type))
                .setMetadata(mapOf(SKJEMATYPE to type.name, UUID_ to callId()))
                .setContentType(APPLICATION_JSON_VALUE).build(), value.toByteArray(UTF_8), kmsKeyName("$key"))
                .also {
                    log.trace(CONFIDENTIAL,
                            "Lagret mellomlagret ${value.jsonPrettify(mapper)} som ${it.name} i bøtte ${mellom.navn}")
                }
        }.name

    override fun les(type: SkjemaType) = les(type, ctx.getFnr())

    fun les(type: SkjemaType, fnr: Fødselsnummer) =
        with(cfg.mellom) {
            with(navn(fnr, type)) {
                lager.get(navn, this)?.let { blob ->
                    String(blob.getContent()).also {
                        log.trace(CONFIDENTIAL, "Lest mellomlagret verdi ${it.jsonPrettify(mapper)} fra $this og bøtte $navn")
                    }
                }
            }
        }

    internal fun String.jsonPrettify(mapper: ObjectMapper) =
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(this, Any::class.java))

    override fun slett(type: SkjemaType) = slett(type, ctx.getFnr())

    fun slett(type: SkjemaType, fnr: Fødselsnummer) =
        with(cfg.mellom) {
            with(navn(fnr, type)) {
                lager.delete(navn, this).also {
                    log.trace(CONFIDENTIAL, "Slettet mellomlagret $this fra bøtte $navn")
                }
            }
        }

    override fun alleBrukere() =
        lager.list(cfg.mellom.navn, BlobListOption.fields(TIME_CREATED))
            .iterateAll().map { log.trace("Entry $it").run { Pair(it.name, LocalDateTime.ofEpochSecond(it.createTime/1000,0,
                    ZoneOffset.UTC).toLocalTime()) } }
}