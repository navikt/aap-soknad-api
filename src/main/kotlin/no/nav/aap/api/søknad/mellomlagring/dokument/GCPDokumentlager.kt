package no.nav.aap.api.søknad.mellomlagring.dokument

import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.CONTENT_TYPE
import com.google.cloud.storage.Storage.BlobField.METADATA
import com.google.cloud.storage.Storage.BlobGetOption.fields
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager.Companion.FILNAVN
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager.Companion.FNR
import no.nav.aap.api.søknad.virus.AttachmentException
import no.nav.aap.api.søknad.virus.VirusScanner
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import java.util.UUID.randomUUID

@ConditionalOnGCP
internal class GCPDokumentlager(@Value("\${mellomlagring.bucket:aap-vedlegg}") private val bøtte: String,
                                private val lager: Storage,
                                private val scanner: VirusScanner,
                                private val typeSjekker: TypeSjekker) : Dokumentlager {

    val log = LoggerUtil.getLogger(javaClass)
    override fun lagreDokument(fnr: Fødselsnummer, bytes: ByteArray, contentType: String?, originalFilename: String?) =
        randomUUID().apply {
            typeSjekker.sjekkType(bytes, contentType, originalFilename)
            scanner.scan(bytes, originalFilename)
            lager.create(newBuilder(of(bøtte, key(fnr, this)))
                .setContentType(contentType)
                .setMetadata(mapOf(FILNAVN to originalFilename, FNR to fnr.fnr))
                .build(), bytes)
                .also { log.trace("Lagret $originalFilename med uuid $this og contentType $contentType") }
        }

    override fun lesDokument(fnr: Fødselsnummer, uuid: UUID) =
        lager.get(bøtte, key(fnr, uuid), fields(METADATA, CONTENT_TYPE))?.let {
            if (fnr.fnr != it.metadata[FNR]) {
                throw JwtTokenUnauthorizedException("Dokumentet med id $uuid er ikke eid av ${fnr.fnr}")
            }
            DokumentInfo(it.getContent(),
                    it.contentType,
                    it.metadata[FILNAVN]).also {
                log.trace("Lest dokument med uuid $uuid er $it")
            }
        }

    override fun slettDokument(uuid: UUID, fnr: Fødselsnummer) =
        lager.delete(of(bøtte, key(fnr, uuid)))

    @Component
    internal class TypeSjekker(@Value("#{\${mellomlager.types :{'application/pdf','image/jpeg','image/png'}}}")
                               private val contentTypes: Set<String>) {

        fun sjekkType(bytes: ByteArray, contentType: String?, originalFilename: String?) =
            with(TIKA.detect(bytes)) {
                if (this != contentType) {
                    throw AttachmentException("Type $this matcher ikke oppgitt $contentType for $originalFilename")
                }
            }.also {
                if (!contentTypes.contains(contentType)) {
                    throw AttachmentException("Type $contentType er ikke blant $contentTypes for $originalFilename")
                }
            }
    }

    companion object {
        private val TIKA = Tika()
    }
}