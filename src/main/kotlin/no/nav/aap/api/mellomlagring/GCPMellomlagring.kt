package no.nav.aap.api.mellomlagring

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.ConditionalOnGCP
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import javax.servlet.http.HttpServletResponse.SC_NOT_FOUND


@ConditionalOnGCP
class GCPMellomlagring(@Value("\${mellomlagring.utland.bucket:aap-utland-mellomlagring}") val bøttenavn: String,
                       val storage: Storage) : Mellomlagring {
    private val log = getLogger(GCPMellomlagring::class.java)

    override fun lagre(katalog: String, key: String, value: String) {
        storage.create(
                BlobInfo.newBuilder(blobFra(katalog, key))
                    .setContentType(APPLICATION_JSON_VALUE).build(), value.toByteArray(Charsets.UTF_8))
    }


    override fun les(katalog: String, key: String): String? {
        return try {
            return storage.get(bøttenavn, key(katalog, key))?.getContent()?.let { String(it) }
        } catch (e: StorageException) {
            if (SC_NOT_FOUND === e.code) {
                log.trace("Katalog {} ikke funnet, ({})", katalog, e)
                null
            }
            log.warn("Katalog {} ikke funnet, ({})", katalog, e.code, e)
            throw e
        }
    }

    override fun slett(katalog: String, key: String) = storage.delete(blobFra(katalog, key))
    private fun key(directory: String, key: String) = directory + "_" + key
    private fun blobFra(katalog: String, key: String) = BlobId.of(bøttenavn, key(katalog, key))

}