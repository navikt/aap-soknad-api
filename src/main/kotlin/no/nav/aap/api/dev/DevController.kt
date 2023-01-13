package no.nav.aap.api.dev

import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.oppslag.søknad.SøknadClient
import no.nav.aap.api.saksbehandling.SaksbehandlingController.VedleggEtterspørsel
import no.nav.aap.api.søknad.fordeling.SøknadVLFordeler
import no.nav.aap.api.søknad.fordeling.VLFordelingConfig
import no.nav.aap.api.søknad.mellomlagring.GCPKryptertMellomlager
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.GCPKryptertDokumentlager
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideRepositories
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile

@UnprotectedRestController(["/dev/"])
@ConditionalOnNotProd
internal class DevController(private val dokumentLager: GCPKryptertDokumentlager,
                             private val mellomlager: GCPKryptertMellomlager,
                             private val cfg: VLFordelingConfig,
                             private val vl: SøknadVLFordeler,
                             private val dittNav: MinSideClient,
                             private val søknad: SøknadClient,
                             private val repos: MinSideRepositories) {

    @GetMapping("/soknader")
    fun søknader(@RequestParam fnr: Fødselsnummer, @SortDefault(sort = ["created"], direction = DESC) @PageableDefault(size = 100) pageable: Pageable) = søknad.søknader(fnr, pageable)

    @GetMapping("/dittnav/avsluttbeskjed")
    fun avsluttBeskjed(@RequestParam fnr: Fødselsnummer, @RequestParam uuid: UUID) = dittNav.avsluttBeskjed(fnr, uuid)

    //@GetMapping("/dittnav/avsluttoppgave")
    //fun avsluttOppgave(@RequestParam fnr: Fødselsnummer, @RequestParam uuid: UUID) = dittNav.avsluttOppgave(fnr, uuid)

    @PostMapping("vl/{fnr}")
    @ResponseStatus(CREATED)
    fun vl(@PathVariable fnr: Fødselsnummer, @RequestBody søknad: StandardSøknad) = vl.fordel(søknad, fnr, "42", cfg.standard)

    @PostMapping("sb/vedlegg")
    @ResponseStatus(CREATED)
    fun vedlegg(@RequestBody e: VedleggEtterspørsel) = søknad.etterspørrVedlegg(e)


    @DeleteMapping("mellomlager/{type}/{fnr}")
    fun slettMellomlagret(@PathVariable type: SkjemaType, @PathVariable fnr: Fødselsnummer): ResponseEntity<Void> = if (mellomlager.slett(type, fnr)) noContent().build() else notFound().build()

    @GetMapping("mellomlager/{type}/{fnr}")
    fun lesMellomlagret(@PathVariable type: SkjemaType, @PathVariable fnr: Fødselsnummer) = mellomlager.les(type, fnr)?.let { ok(it) } ?: notFound().build()

    @PostMapping("mellomlager/{type}/{fnr}", produces = [TEXT_PLAIN_VALUE])
    @ResponseStatus(CREATED)
    fun mellomlagre(@PathVariable type: SkjemaType, @PathVariable fnr: Fødselsnummer, @RequestBody data: String) = mellomlager.lagre(data, type, fnr)

    @PostMapping("vedlegg/lagre/{fnr}", consumes = [MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(CREATED)
    fun lagreDokument(@PathVariable fnr: Fødselsnummer, @RequestPart("vedlegg") vedlegg: MultipartFile) =
        with(vedlegg) {
            dokumentLager.lagreDokument(DokumentInfo(bytes, originalFilename, contentType), fnr)
        }

    @DeleteMapping("vedlegg/slett/{fnr}")
    @ResponseStatus(NO_CONTENT)
    fun slettDokument(@PathVariable fnr: Fødselsnummer, @RequestParam vararg uuids: UUID?) = dokumentLager.slettDokumenter(uuids.toList(), fnr)

    @DeleteMapping("vedlegg/slettAlle/{fnr}")
    @ResponseStatus(NO_CONTENT)
    fun slettAlleDokumenter(@PathVariable fnr: Fødselsnummer) = dokumentLager.slettAlleDokumenter(fnr)
}