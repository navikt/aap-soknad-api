package no.nav.aap.api.mellomlagring

import no.nav.aap.api.mellomlagring.Vedlegg.Companion.FILNAVN
import no.nav.aap.api.mellomlagring.Vedlegg.Companion.FNR
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
import org.springframework.http.ResponseEntity.*


@ProtectedRestController(value = ["vedlegg"], issuer = IDPORTEN)
class VedleggController(private val bucket: GCPVedlegg, private val ctx: AuthContext) {

    @PostMapping(value = ["/lagre"], consumes = [MULTIPART_FORM_DATA_VALUE])
    fun lagreVedlegg(@RequestPart("vedlegg") vedlegg: MultipartFile): ResponseEntity<Void> =
        status(CREATED)
            .header(LOCATION, "${bucket.lagreVedlegg(ctx.getFnr(), vedlegg)}")
            .build()

        @GetMapping("/les/{uuid}")
    fun lesVedlegg(@PathVariable uuid: UUID)  =
        bucket.lesVedlegg(ctx.getFnr(), uuid)
            ?.let {
                with(it) {
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
            } ?: notFound().build()

    @DeleteMapping("/slett/{uuid}")
    fun slettVedlegg(@PathVariable uuid: UUID): ResponseEntity<Void> =
        if (bucket.slettVedlegg(ctx.getFnr(),uuid)) noContent().build() else notFound().build()
}