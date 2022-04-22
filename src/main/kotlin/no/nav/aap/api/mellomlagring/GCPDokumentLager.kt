package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId.of
import com.google.cloud.storage.BlobInfo.newBuilder
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobField.CONTENT_TYPE
import com.google.cloud.storage.Storage.BlobField.METADATA
import com.google.cloud.storage.Storage.BlobGetOption.fields
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.mellomlagring.DokumentLager.Companion.FILNAVN
import no.nav.aap.api.mellomlagring.DokumentLager.Companion.FNR
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import java.util.*
import java.util.UUID.randomUUID


@ConditionalOnGCP
internal class GCPDokumentLager(@Value("\${mellomlagring.bucket:aap-vedlegg}") private val bøtte: String,
                                private val storage: Storage) : DokumentLager {

    val log = LoggerUtil.getLogger(javaClass)
    override fun lagreDokument(fnr: Fødselsnummer, bytes: ByteArray, contentType: String?, originalFilename: String?) =
        randomUUID().also {
            storage.create(newBuilder(of(bøtte, key(fnr, it)))
                .setContentType(contentType)
                .setMetadata(mapOf(FILNAVN to originalFilename, FNR to fnr.fnr))
                .build(), bytes)
                .also { log.trace("Lagret vedlegg som ${it.blobId.toGsUtilUri()}") }
        }
    override fun lesDokument(fnr: Fødselsnummer, uuid: UUID) =
        storage.get(bøtte, key(fnr, uuid), fields(METADATA, CONTENT_TYPE))
    override fun slettDokument(fnr: Fødselsnummer, uuid: UUID) =
        storage.delete(of(bøtte, key(fnr, uuid)))

}