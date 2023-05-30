package no.nav.aap.api.oppslag

import io.micrometer.observation.annotation.Observed
import java.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.SortDefault
import org.springframework.http.CacheControl.noCache
import org.springframework.http.ContentDisposition.attachment
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import no.nav.aap.api.oppslag.OppslagController.Companion.OPPSLAG_BASE
import no.nav.aap.api.oppslag.arbeid.ArbeidClient
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagClient
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.kontaktinformasjon.KRRClient
import no.nav.aap.api.oppslag.konto.KontoClient
import no.nav.aap.api.oppslag.person.PDLClient
import no.nav.aap.api.oppslag.søknad.SøknadClient
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker.Companion.TIKA
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity.Companion.CREATED
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.spring.ProtectedRestController

@ProtectedRestController(value = [OPPSLAG_BASE], issuer = IDPORTEN)
@Observed(contextualName = "oppslag")
class OppslagController(
    val pdl : PDLClient,
    val behandler : BehandlerClient,
    val arbeid : ArbeidClient,
    val krr : KRRClient,
    val søknad : SøknadClient,
    val konto : KontoClient,
    val arkiv : ArkivOppslagClient,
    private val ctx: TokenValidationContextHolder) {

    val log = getLogger(javaClass)

    @GetMapping("/soeker")
    fun søker() : SøkerInfo {
       // val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
       // val validationContext = ctx.tokenValidationContext  // pass this ?
        runBlocking {
            coroutineScope {
                //SpringTokenValidationContextHolder().tokenValidationContext = validationContext
                log.trace("ASYNC start")
                val start = System.currentTimeMillis()
                val a = async {  behandler.behandlerInfo() }.await()
                val k  = async { krr.kontaktInfo()}.await()
               val b  = async { pdl.søkerMedBarn() }.await()
                val k1  = async { konto.kontoInfo() }.await()
                val a1  = async { arbeid.arbeidInfo() }.await()
                log.trace("ASYNC all running")
                val si = SøkerInfo(b,a,a1,k,k1)
                val d = System.currentTimeMillis() - start
                log.trace("ASYNC end {} etter {} ms", si, d)
            }
        }
        log.trace("SYNC start")
        val start = System.currentTimeMillis()
        return SøkerInfo(pdl.søkerMedBarn(), behandler.behandlerInfo(), arbeid.arbeidInfo(), krr.kontaktInfo(), konto.kontoInfo()).also {
            val d = System.currentTimeMillis() - start
            log.trace("SYNC end etter $d ms")
        }
    }

    @GetMapping("/soekermedbarn")
    fun søkerMedBarn() = pdl.søkerMedBarn()

    @GetMapping("/soekerutenbarn")
    fun søkerUtenBarn() = pdl.søkerUtenBarn()

    @GetMapping("/kontonummer")
    fun kontonummer() = konto.kontoInfo() ?: notFound()

    @GetMapping("/behandlere")
    fun behandlere() = behandler.behandlerInfo()

    @GetMapping("/krr")
    fun krr() = krr.kontaktInfo()

    @GetMapping("/dokumenter")
    fun dokumenter() = arkiv.dokumenter()

    @GetMapping("/soeknader")
    fun søknader(@SortDefault(sort = [CREATED], direction = DESC) @PageableDefault(size = 100) pageable : Pageable) =
        søknad.søknader(pageable)

    @GetMapping("/soeknad/{uuid}")
    fun søknadForUUID(@PathVariable uuid : UUID) = søknad.søknad(uuid)

    @GetMapping("/soeknad/journalpost/{journalpostId}", produces = [APPLICATION_PDF_VALUE])
    fun søknadForJournalpost(@PathVariable journalpostId : String) =
        dokument(journalpostId, arkiv.søknadDokumentId(journalpostId))

    @GetMapping(DOKUMENT, produces = [APPLICATION_PDF_VALUE])
    fun dokument(@PathVariable journalpostId : String, @PathVariable dokumentId : String) =
        arkiv.dokument(journalpostId, dokumentId)
            .let {
                TIKA.detect(it).also { type ->
                    if (APPLICATION_PDF_VALUE != type) {
                        log.warn("Content type for $journalpostId/$dokumentId er $type, forventet $APPLICATION_PDF_VALUE")
                    }
                }
                ok()
                    .contentType(APPLICATION_PDF)
                    .cacheControl(noCache().mustRevalidate())
                    .headers(HttpHeaders().apply {
                        contentDisposition = attachment().filename("$journalpostId-$dokumentId.pdf")
                            .build()
                    })
                    .body(it)
            }

    companion object {

        const val OPPSLAG_BASE = "/oppslag"
        private const val DOKUMENT = "/dokument/{journalpostId}/{dokumentId}"
    }
}