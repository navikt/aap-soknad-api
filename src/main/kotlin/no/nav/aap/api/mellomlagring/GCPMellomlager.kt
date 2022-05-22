package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.METADATA
import com.google.cloud.storage.Storage.BlobGetOption.fields
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.mellomlagring.Dokumentlager.Companion.FNR
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.nio.charset.StandardCharsets.UTF_8

@ConditionalOnGCP
internal class GCPMellomlager(@Value("\${mellomlagring.bucket:aap-mellomlagring}") private val bøtte: String,
                              private val lager: Storage) : Mellomlager {

    val log = LoggerUtil.getLogger(javaClass)
    override fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String) =
        lager.create(newBuilder(of(bøtte, key(fnr, type)))
            .setMetadata(mapOf(FNR to fnr.fnr))
            .setContentType(APPLICATION_JSON_VALUE).build(),
                value.toByteArray(UTF_8))
            .blobId.toGsUtilUri()
            .also { log.trace(CONFIDENTIAL, "Lagret $type for $fnr") }

    override fun les(fnr: Fødselsnummer, type: SkjemaType) =
        lager.get(bøtte, key(fnr, type), fields(METADATA))?.let {
            if (fnr.fnr != it.metadata[FNR]) {
                throw JwtTokenUnauthorizedException("Dokumentet er ikke eid av ${fnr.fnr}")
            }
            String(it.getContent(), UTF_8)
        }

    override fun slett(fnr: Fødselsnummer, type: SkjemaType) =
        lager.delete(of(bøtte, key(fnr, type)))
}