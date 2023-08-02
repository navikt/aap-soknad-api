package no.nav.aap.api.søknad.mellomlagring.dokument

import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentlagerController.Companion.BASEPATH
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.CacheControl.noCache
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.MediaType.parseMediaType
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile
import java.util.*

@ProtectedRestController(value = [BASEPATH], issuer = IDPORTEN)
internal class DokumentlagerController(private val lager: Dokumentlager) {

    @PostMapping("/lagre", consumes = [MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(CREATED)
    fun lagreDokument(@RequestPart("vedlegg") vedlegg: MultipartFile) {
        val vedleggContentType = vedlegg.contentType
        if (vedleggContentType == null) {
            lager.lagreDokument(DokumentInfo(vedlegg.bytes, vedlegg.originalFilename))
        } else {
            lager.lagreDokument(DokumentInfo(vedlegg.bytes, vedlegg.originalFilename, vedleggContentType))
        }
    }

    @GetMapping("/les/{uuid}")
    fun lesDokument(@PathVariable uuid: UUID) =
        lager.lesDokument(uuid)
            ?.let {
                ok()
                    .contentType(parseMediaType(it.contentType))
                    .cacheControl(noCache().mustRevalidate())
                    .headers(HttpHeaders().apply {
                        it.contentDisposition?.let { cd ->
                            contentDisposition = cd
                        }
                    })
                    .body(it.bytes)

            } ?: notFound().build()

    @DeleteMapping("/slett")
    @ResponseStatus(NO_CONTENT)
    fun slettDokumenter(@RequestParam vararg uuids: UUID) = lager.slettDokumenter(uuids.toList())

    @DeleteMapping("/slettAlle")
    @ResponseStatus(NO_CONTENT)
    fun slettAlleDokumenter() =
        lager.slettAlleDokumenter()

    companion object {
        const val BASEPATH = "vedlegg"
    }
}