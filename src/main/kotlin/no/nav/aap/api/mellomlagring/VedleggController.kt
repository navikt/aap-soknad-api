package no.nav.aap.api.mellomlagring

import no.nav.aap.api.mellomlagring.GCPVedlegg.Companion.FILNAVN
import no.nav.aap.api.mellomlagring.GCPVedlegg.Companion.FNR
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.CacheControl.noCache
import org.springframework.http.ContentDisposition.attachment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.*
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import org.springframework.http.MediaType.parseMediaType
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok


@ProtectedRestController(value = ["vedlegg"], issuer = IDPORTEN)
class VedleggController(private val bucket: GCPVedlegg, private val ctx: AuthContext) {

    @PostMapping(value = ["/lagre"], consumes = [MULTIPART_FORM_DATA_VALUE])
    fun lagreVedlegg(@RequestPart("vedlegg") file: MultipartFile): ResponseEntity<Void> {
        val uuid = bucket.lagreVedlegg(ctx.getFnr(), file)
        return ResponseEntity.status(CREATED).header(LOCATION,"$uuid").build()
    }

        @GetMapping("/les/{uuid}")
    fun lesVedlegg(@PathVariable uuid: UUID)  =
        bucket.lesVedlegg(ctx.getFnr(), uuid)
            ?.let { vedlegg ->
                with(vedlegg) {
                    if (ctx.getFnr().fnr != metadata[FNR]) {
                        throw JwtTokenUnauthorizedException("Dokumentet med id $uuid er ikke eid av ${ctx.getFnr()}")
                    }
                    ok()
                        .contentType(parseMediaType(contentType))
                        .cacheControl(noCache().mustRevalidate())
                        .headers(HttpHeaders().apply {
                            contentDisposition = attachment().filename(metadata[FILNAVN]!!).build() })
                        .body(getContent())
                }
            } ?: notFound()

    @DeleteMapping("/slett/{uuid}")
    fun slettVedlegg(@PathVariable uuid: UUID): ResponseEntity<Void> {
        bucket.slettVedlegg(ctx.getFnr(),uuid)
        return ResponseEntity<Void>(NO_CONTENT)
    }
}