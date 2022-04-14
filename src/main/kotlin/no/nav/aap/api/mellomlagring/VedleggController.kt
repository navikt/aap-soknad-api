package no.nav.aap.api.mellomlagring

import no.nav.aap.api.mellomlagring.GCPVedlegg.Companion.FILNAVN
import no.nav.aap.api.mellomlagring.GCPVedlegg.Companion.FNR
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.CacheControl
import org.springframework.http.CacheControl.noCache
import org.springframework.http.ContentDisposition
import org.springframework.http.ContentDisposition.attachment
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


@ProtectedRestController(value = ["vedlegg"], issuer = IDPORTEN)
class VedleggController(private val vedlegg: GCPVedlegg, private val ctx: AuthContext) {

    @PostMapping(value = ["/lagre"], consumes = [MULTIPART_FORM_DATA_VALUE])
    fun lagreVedlegg(@RequestPart("vedlegg") file: MultipartFile) = ResponseEntity<UUID>(vedlegg.lagreVedlegg(ctx.getFnr(), file), CREATED)

    @GetMapping("/les/{uuid}")
    fun lesVedlegg(@PathVariable uuid: UUID)  =
        vedlegg.lesVedlegg(ctx.getFnr(), uuid)?.let {
            if (ctx.getFnr().fnr != it.metadata[FNR]) {
               throw JwtTokenUnauthorizedException("Dokumentet med id $uuid er ikke eid av ${ctx.getFnr()}")
            }
            ResponseEntity.ok()
                .contentType(parseMediaType(it.contentType))
                .cacheControl(noCache().mustRevalidate())
                .headers(HttpHeaders().apply {
                    contentDisposition = attachment().filename(it.metadata[FILNAVN]!!).build(),
            })
                .body(it.getContent())
        } ?: ResponseEntity.notFound()

    @DeleteMapping("/slett/{uuid}")
    fun slettVedlegg(@PathVariable uuid: UUID): ResponseEntity<Void> {
        vedlegg.slettVedlegg(ctx.getFnr(),uuid)
        return ResponseEntity<Void>(NO_CONTENT)
    }
}