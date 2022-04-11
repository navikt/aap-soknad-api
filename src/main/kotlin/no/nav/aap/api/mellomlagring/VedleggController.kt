package no.nav.aap.api.mellomlagring

import no.nav.aap.api.mellomlagring.GCPVedlegg.Companion
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpHeaders.CACHE_CONTROL
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.HttpHeaders.EXPIRES
import org.springframework.http.HttpHeaders.PRAGMA
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.parseMediaType
import org.springframework.util.MimeTypeUtils.parseMimeType


@ProtectedRestController(value = ["vedlegg"], issuer = IDPORTEN)
class VedleggController(private val vedlegg: GCPVedlegg, private val ctx: AuthContext) {

    @PostMapping(value = ["/lagre"], consumes = [MULTIPART_FORM_DATA_VALUE])
    fun lagreVedlegg(@RequestPart("vedlegg") file: MultipartFile): ResponseEntity<UUID> {
        val uuid  = vedlegg.lagre(ctx.getFnr(), file)
        return ResponseEntity<UUID>(uuid, CREATED)
    }
    @GetMapping("/les/{uuid}")
    fun lesVedlegg(@PathVariable uuid: UUID)  : ResponseEntity<ByteArray>? {
        val data = vedlegg.les(ctx.getFnr(), uuid)
        return data?.let {  ResponseEntity<ByteArray>(
                data.getContent(),
                HttpHeaders().apply {
                    add(EXPIRES, "0")
                    add(PRAGMA, "no-cache")
                    add(CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    add(CONTENT_DISPOSITION,
                            "attachment; filename=${data.metadata[GCPVedlegg.FILNAVN]}")
                    contentType = parseMediaType(data.contentType)
                },
                OK)} ?: ResponseEntity<ByteArray>(NOT_FOUND)
    }
    @DeleteMapping("/slett/{uuid}")
    fun slettVedlegg(@PathVariable uuid: UUID): ResponseEntity<Void> {
        vedlegg.slett(ctx.getFnr(),uuid)
        return ResponseEntity<Void>(NO_CONTENT)
    }
}