package no.nav.aap.api.dev

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.mellomlagring.Mellomlagring
import no.nav.aap.api.mellomlagring.Vedlegg
import no.nav.aap.api.mellomlagring.Vedlegg.Companion.FILNAVN
import no.nav.aap.api.mellomlagring.Vedlegg.Companion.FNR
import no.nav.aap.api.søknad.SkjemaType
import no.nav.aap.api.søknad.dittnav.DittNavFormidler
import no.nav.aap.api.søknad.joark.pdf.PDFGeneratorWebClientAdapter
import no.nav.aap.api.søknad.joark.pdf.PDFGeneratorWebClientAdapter.StandardPDFData
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.CacheControl.noCache
import org.springframework.http.ContentDisposition.attachment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Unprotected
@RestController
@RequestMapping(value = ["/dev/"])
@ConditionalOnDevOrLocal
internal class DevController(private val vedleggLager: Vedlegg,
                             private val mellomlagring: Mellomlagring,
                             private val dittnav: DittNavFormidler,
                             private val pdf: PDFGeneratorWebClientAdapter) {

    @PostMapping(value = ["generate"], produces = [APPLICATION_PDF_VALUE])
    fun pdfGen(@RequestBody data: StandardPDFData) =
        ok()
            .headers(HttpHeaders()
                .apply {
                    contentDisposition = attachment().filename("pdfgen.pdf").build()
                })
            .body(pdf.generate(data))

    @DeleteMapping("/lager/{type}/{fnr}")
    fun slettmellomlager(@PathVariable type: SkjemaType,@PathVariable fnr: Fødselsnummer): ResponseEntity<Void> =
        if (mellomlagring.slett(fnr,type)) noContent().build() else notFound().build()
    @GetMapping("/lager/{type}/{fnr}")
    fun lesmellomlager(@PathVariable type: SkjemaType,@PathVariable fnr: Fødselsnummer) = mellomlagring.les(fnr, type)
    @PostMapping("/lager/{type}/{fnr}")
    @ResponseStatus(CREATED)
    fun mellomlagre(@PathVariable type: SkjemaType, @PathVariable fnr: Fødselsnummer, @RequestBody data: String) = mellomlagring.lagre(fnr, type, data)

    @PostMapping(value = ["dittnav/beskjed/{fnr}"])
    @ResponseStatus(CREATED)
    fun opprettBeskjed(@PathVariable fnr: Fødselsnummer) = dittnav.opprettBeskjed(fnr)

    @PostMapping(value = ["lagre/{fnr}"], consumes = [MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(CREATED)
    fun lagreVedlegg(@PathVariable fnr: Fødselsnummer, @RequestPart("vedlegg") vedlegg: MultipartFile) = vedleggLager.lagreVedlegg(fnr, vedlegg)

    @GetMapping(path = ["les/{fnr}/{uuid}"])
    fun lesVedlegg(@PathVariable fnr: Fødselsnummer, @PathVariable uuid: UUID) =
        vedleggLager.lesVedlegg(fnr, uuid)
            ?.let {
                with(it) {
                    if (fnr.fnr != metadata[FNR]) {
                        throw JwtTokenUnauthorizedException("Dokumentet med id $uuid er ikke eid av $fnr.fnr")
                    }
                    ok()
                        .contentType(parseMediaType(contentType))
                        .cacheControl(noCache().mustRevalidate())
                        .headers(HttpHeaders()
                            .apply {
                                contentDisposition = attachment().filename(metadata[FILNAVN]!!).build()
                            })
                        .body(getContent())
                }
            } ?: notFound().build()

    @DeleteMapping("slett/{fnr}/{uuid}")
    fun slettVedlegg(@PathVariable fnr: Fødselsnummer, @PathVariable uuid: UUID): ResponseEntity<Void> =
        if (vedleggLager.slettVedlegg(fnr, uuid)) noContent().build() else notFound().build()
}