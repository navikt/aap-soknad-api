package no.nav.aap.api.dev

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.oppslag.søknad.SøknadClient
import no.nav.aap.api.søknad.ettersendelse.Ettersending
import no.nav.aap.api.søknad.fordeling.StandardSøknadFordeler.StandardSøknadFullfører
import no.nav.aap.api.søknad.fordeling.SøknadVLFordeler
import no.nav.aap.api.søknad.fordeling.VLFordelingConfig
import no.nav.aap.api.søknad.joark.JoarkFordeler.JoarkEttersendingResultat
import no.nav.aap.api.søknad.joark.JoarkJournalpostGenerator
import no.nav.aap.api.søknad.mellomlagring.GCPKryptertMellomlager
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.GCPKryptertDokumentlager
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideRepositories
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.http.CacheControl.noCache
import org.springframework.http.ContentDisposition.attachment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.MediaType.parseMediaType
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
import java.util.*

@UnprotectedRestController(["/dev/"])
@ConditionalOnNotProd
internal class DevController(private val dokumentLager: GCPKryptertDokumentlager,
                             private val mellomlager: GCPKryptertMellomlager,
                             private val cfg: VLFordelingConfig,
                             private val vl: SøknadVLFordeler,
                             private val dittNav: MinSideClient,
                             private val søknad: SøknadClient,
                             private val fullfører: StandardSøknadFullfører,
                             private val joark: JoarkJournalpostGenerator,
                             private val repos: MinSideRepositories) {

    private val log = LoggerUtil.getLogger(javaClass)

    @GetMapping("/soknader")
    fun søknader(@RequestParam fnr: Fødselsnummer) =
        søknad.søknader(fnr)

    @PostMapping("ettersend/{fnr}")
    @ResponseStatus(CREATED)
    fun ettersend(@PathVariable fnr: Fødselsnummer, @RequestBody ettersending: Ettersending) {
        log.trace("Mottok ettersendng $ettersending for $fnr")
        val søker = Søker(Navn("Dennis", "B", "Bergkamp"), fnr)
        val post = joark.journalpostFra(ettersending, søker)
        log.trace("Lagde journalpost $post for $fnr")
        fullfører.fullfør(ettersending, søker.fnr, JoarkEttersendingResultat("42"))
    }

    @GetMapping("/dittnav/avsluttalle")
    fun avsluttAlle(@RequestParam fnr: Fødselsnummer) {
        with(repos) {
            beskjeder.findByFnrAndDoneIsFalse(fnr.fnr).map { it.eventid }.forEach { avsluttBeskjed(fnr, it) }
            oppgaver.findByFnrAndDoneIsFalse(fnr.fnr).map { it.eventid }.forEach { avsluttOppgave(fnr, it) }
        }
    }

    @GetMapping("/dittnav/avsluttbeskjed")
    fun avsluttBeskjed(@RequestParam fnr: Fødselsnummer, @RequestParam uuid: UUID) =
        dittNav.avsluttBeskjed(STANDARD, fnr, uuid)

    @GetMapping("/dittnav/avsluttoppgave")
    fun avsluttOppgave(@RequestParam fnr: Fødselsnummer, @RequestParam uuid: UUID) =
        dittNav.avsluttOppgave(STANDARD, fnr, uuid)

    @PostMapping("vl/{fnr}")
    @ResponseStatus(CREATED)
    fun vl(@PathVariable fnr: Fødselsnummer, @RequestBody søknad: StandardSøknad) =
        vl.fordel(søknad, fnr, "42", cfg.standard)

    @DeleteMapping("mellomlager/{type}/{fnr}")
    fun slettMellomlagret(@PathVariable type: SkjemaType, @PathVariable fnr: Fødselsnummer): ResponseEntity<Void> =
        if (mellomlager.slett(type, fnr)) noContent().build() else notFound().build()

    @GetMapping("mellomlager/{type}/{fnr}")
    fun lesMellomlagret(@PathVariable type: SkjemaType, @PathVariable fnr: Fødselsnummer) =
        mellomlager.les(type, fnr)?.let { ok(it) } ?: notFound().build()

    @PostMapping("mellomlager/{type}/{fnr}", produces = [TEXT_PLAIN_VALUE])
    @ResponseStatus(CREATED)
    fun mellomlagre(@PathVariable type: SkjemaType, @PathVariable fnr: Fødselsnummer, @RequestBody data: String) =
        mellomlager.lagre(data, type, fnr)

    @PostMapping("vedlegg/lagre/{fnr}", consumes = [MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(CREATED)
    fun lagreDokument(@PathVariable fnr: Fødselsnummer, @RequestPart("vedlegg") vedlegg: MultipartFile) =
        with(vedlegg) {
            dokumentLager.lagreDokument(DokumentInfo(bytes, contentType, originalFilename), fnr)
        }

    @GetMapping("vedlegg/les/{fnr}/{uuid}")
    fun lesDokument(@PathVariable fnr: Fødselsnummer, @PathVariable uuid: UUID) =
        dokumentLager.lesDokument(uuid, fnr)
            ?.let {
                ok().contentType(parseMediaType(it.contentType!!))
                    .cacheControl(noCache().mustRevalidate())
                    .headers(HttpHeaders()
                        .apply {
                            contentDisposition = attachment().filename(it.filnavn!!).build()
                        })
                    .body(it.bytes)
            } ?: notFound().build()

    @DeleteMapping("vedlegg/slett/{fnr}")
    @ResponseStatus(NO_CONTENT)
    fun slettDokument(@PathVariable fnr: Fødselsnummer, @RequestParam vararg uuids: UUID) =
        dokumentLager.slettUUIDs(uuids.toList(), fnr)
}