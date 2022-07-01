package no.nav.aap.api.dev

import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.mellomlagring.Mellomlager
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.routing.standard.StandardSøknadVLRouter
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.http.CacheControl.noCache
import org.springframework.http.ContentDisposition.attachment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
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
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate.now
import java.util.*

@UnprotectedRestController(["/dev/"])
@ConditionalOnNotProd
internal class DevController(private val dokumentLager: Dokumentlager,
                             private val mellomlager: Mellomlager,
                             private val vl: StandardSøknadVLRouter) {

    @PostMapping("vl/{fnr}")
    @ResponseStatus(CREATED)
    fun vl(@PathVariable fnr: Fødselsnummer, @RequestBody søknad: StandardSøknad) =
        vl.route(søknad, Søker(Navn("Ole", "B", "Olsen"), fnr,
                Adresse("Gata", "A", "14", PostNummer("2600", "Lillehammer")),
                now(), listOf()),
                "42")

    @DeleteMapping("mellomlager/{type}/{fnr}")
    fun slettMellomlagret(@PathVariable type: SkjemaType, @PathVariable fnr: Fødselsnummer): ResponseEntity<Void> =
        if (mellomlager.slett(fnr, type)) noContent().build() else notFound().build()

    @GetMapping("mellomlager/{type}/{fnr}")
    fun lesMellomlagret(@PathVariable type: SkjemaType, @PathVariable fnr: Fødselsnummer) =
        mellomlager.les(fnr, type)?.let { ok(it) } ?: notFound().build()

    @PostMapping("mellomlager/{type}/{fnr}", produces = [TEXT_PLAIN_VALUE])
    @ResponseStatus(CREATED)
    fun mellomlagre(@PathVariable type: SkjemaType, @PathVariable fnr: Fødselsnummer, @RequestBody data: String) =
        mellomlager.lagre(fnr, type, data)

    @PostMapping("vedlegg/lagre/{fnr}", consumes = [MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(CREATED)
    fun lagreDokument(@PathVariable fnr: Fødselsnummer, @RequestPart("vedlegg") vedlegg: MultipartFile) =
        with(vedlegg) {
            dokumentLager.lagreDokument(fnr, DokumentInfo(bytes, contentType, originalFilename))
        }

    @GetMapping("vedlegg/les/{fnr}/{uuid}")
    fun lesDokument(@PathVariable fnr: Fødselsnummer, @PathVariable uuid: UUID) =
        dokumentLager.lesDokument(fnr, uuid)
            ?.let {
                ok().contentType(parseMediaType(it.contentType!!))
                    .cacheControl(noCache().mustRevalidate())
                    .headers(HttpHeaders()
                        .apply {
                            contentDisposition = attachment().filename(it.filnavn!!).build()
                        })
                    .body(it.bytes)
            } ?: notFound().build()

    @DeleteMapping("vedlegg/slett/{fnr}/{uuid}")
    @ResponseStatus(NO_CONTENT)
    fun slettDokument(@PathVariable fnr: Fødselsnummer, @PathVariable uuid: UUID) =
        dokumentLager.slettDokument(fnr, uuid)
}