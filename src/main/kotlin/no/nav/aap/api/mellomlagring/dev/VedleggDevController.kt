package no.nav.aap.api.mellomlagring.dev

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.mellomlagring.GCPVedlegg
import no.nav.aap.api.mellomlagring.GCPVedlegg.Companion.FILNAVN
import no.nav.aap.api.mellomlagring.GCPVedlegg.Companion.FNR
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.CacheControl.noCache
import org.springframework.http.ContentDisposition.attachment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.LOCATION
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.MediaType.parseMediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID



@Unprotected
@RestController
@RequestMapping(value= ["/dev/vedlegg/"])
class VedleggDevController(private val bucket: GCPVedlegg) {


    @PostMapping(value = ["lagre/{fnr}"], consumes = [MULTIPART_FORM_DATA_VALUE])
    fun lagreVedlegg(@PathVariable fnr: Fødselsnummer, @RequestPart("vedlegg") vedlegg: MultipartFile): ResponseEntity<Void> =
        status(CREATED).header(LOCATION, "${bucket.lagreVedlegg(fnr, vedlegg)}").build()

    @GetMapping(path= ["les/{fnr}/{uuid}"])
    fun lesVedlegg(@PathVariable fnr: Fødselsnummer,@PathVariable uuid: UUID) =
        bucket.lesVedlegg(fnr, uuid)
            ?.let {
                with(it) {
                    if (fnr.fnr != metadata[FNR]) {
                        throw JwtTokenUnauthorizedException("Dokumentet med id $uuid er ikke eid av $fnr.fnr")
                    }
                    ok()
                        .contentType(parseMediaType(contentType))
                        .cacheControl(noCache().mustRevalidate())
                        .headers(HttpHeaders().apply {
                            contentDisposition = attachment().filename(metadata[FILNAVN]!!).build() })
                        .body(getContent())
                }
            } ?: notFound().build()

    @DeleteMapping("slett/{fnr}/{uuid}")
    fun slettVedlegg(@PathVariable fnr: Fødselsnummer,@PathVariable uuid: UUID): ResponseEntity<Void> =
         if (bucket.slettVedlegg(fnr,uuid)) noContent().build() else notFound().build()
}