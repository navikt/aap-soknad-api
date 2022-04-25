package no.nav.aap.api.mellomlagring

import no.nav.aap.api.mellomlagring.Dokumentlager.Companion.FILNAVN
import no.nav.aap.api.mellomlagring.Dokumentlager.Companion.FNR
import no.nav.aap.api.mellomlagring.DokumentlagerController.Companion.BASEPATH
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.CacheControl.noCache
import org.springframework.http.ContentDisposition.attachment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.MediaType.parseMediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile
import java.util.*


@ProtectedRestController(value = [BASEPATH], issuer = IDPORTEN)
internal class DokumentlagerController(private val lager: Dokumentlager, private val ctx: AuthContext) {

    @PostMapping(value = ["/lagre"], consumes = [MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(CREATED)
    fun lagreDokument(@RequestPart("vedlegg") vedlegg: MultipartFile) =
        lager.lagreDokument(ctx.getFnr(), vedlegg)
    @GetMapping("/les/{uuid}")
    fun lesDokument(@PathVariable uuid: UUID) =
        lager.lesDokument(ctx.getFnr(), uuid)
            ?.let {
                with(it) {
                    if (ctx.getFnr().fnr != metadata[FNR]) {
                        throw JwtTokenUnauthorizedException("Dokumentet med id $uuid er ikke eid av ${ctx.getFnr()}")
                    }
                    ok().contentType(parseMediaType(contentType))
                        .cacheControl(noCache().mustRevalidate())
                        .headers(HttpHeaders().apply {
                            contentDisposition = attachment().filename(metadata[FILNAVN]!!).build()
                        })
                        .body(getContent())
                }
            } ?: notFound().build()

    @DeleteMapping("/slett/{uuid}")
    fun slettDokument(@PathVariable uuid: UUID): ResponseEntity<Void> =
        if (lager.slettDokument(ctx.getFnr(), uuid)) noContent().build() else notFound().build()

    companion object {
        const val BASEPATH = "vedlegg"
    }
}