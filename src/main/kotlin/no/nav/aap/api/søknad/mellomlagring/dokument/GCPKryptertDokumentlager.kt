package no.nav.aap.api.søknad.mellomlagring.dokument

import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.CONTENT_TYPE
import com.google.cloud.storage.Storage.BlobField.METADATA
import com.google.cloud.storage.Storage.BlobGetOption.fields
import com.google.crypto.tink.Aead
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.mellomlagring.GCPBucketConfig
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager.Companion.FILNAVN
import no.nav.aap.api.søknad.virus.AttachmentException
import no.nav.aap.api.søknad.virus.VirusScanner
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import java.util.UUID.randomUUID

@ConditionalOnGCP
@Primary
internal class GCPKryptertDokumentlager(private val cfg: GCPBucketConfig,
                                        private val lager: Storage,
                                        private val scanner: VirusScanner,
                                        private val typeSjekker: TypeSjekker,
                                        private val aead: Aead) : Dokumentlager {

    private val log = getLogger(javaClass)
    override fun lagreDokument(fnr: Fødselsnummer, bytes: ByteArray, contentType: String?, originalFilename: String?) =
        randomUUID().apply {
            log.trace("Lagrer $originalFilename kryptert med uuid $this og contentType $contentType")
            typeSjekker.sjekkType(bytes, contentType, originalFilename)
            scanner.scan(bytes, originalFilename)
            lager.create(newBuilder(of(cfg.vedlegg, key(fnr, this)))
                .setContentType(contentType)
                .setMetadata(mapOf(FILNAVN to originalFilename))
                .build(), aead.encrypt(bytes, fnr.fnr.toByteArray(UTF_8)))
                .also { log.trace("Lagret $originalFilename kryptert med uuid $this og contentType $contentType (${bytes.size} bytes") }
        }

    override fun lesDokument(fnr: Fødselsnummer, uuid: UUID) =
        lager.get(cfg.vedlegg, key(fnr, uuid), fields(METADATA, CONTENT_TYPE))?.let {
            DokumentInfo(aead.decrypt(it.getContent(), fnr.fnr.toByteArray(UTF_8)),
                    it.contentType,
                    it.metadata[FILNAVN]).also {
                log.trace("Lest kryptert dokument med uuid $uuid er $it")
            }
        }

    override fun slettDokument(uuid: UUID, fnr: Fødselsnummer) =
        lager.delete(of(cfg.vedlegg, key(fnr, uuid)))

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