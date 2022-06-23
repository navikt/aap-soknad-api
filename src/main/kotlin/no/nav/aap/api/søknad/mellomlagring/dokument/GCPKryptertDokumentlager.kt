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
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
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
                                        private val sjekkere: List<DokumentSjekker>,
                                        private val aead: Aead) : Dokumentlager {

    private val log = getLogger(javaClass)
    override fun lagreDokument(fnr: Fødselsnummer, dokument: DokumentInfo) =
        randomUUID().apply {
            log.trace("Lagrer ${dokument.filnavn} kryptert med uuid $this og contentType ${dokument.contentType}")
            sjekkere.forEach { it.sjekk(dokument) }
            lager.create(newBuilder(of(cfg.vedlegg, key(fnr, this)))
                .setContentType(dokument.contentType)
                .setMetadata(mapOf(FILNAVN to dokument.filnavn))
                .build(), aead.encrypt(dokument.bytes, fnr.fnr.toByteArray(UTF_8)))
                .also { log.trace("Lagret $dokument kryptert med uuid $this") }
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
                               private val contentTypes: Set<String>) : DokumentSjekker {

        override fun sjekk(dokument: DokumentInfo) =
            if (!contentTypes.contains(dokument.contentType)) {
                throw AttachmentException("Type ${dokument.contentType} er ikke blant $contentTypes for ${dokument.filnavn}")
            }
            else Unit
    }
}