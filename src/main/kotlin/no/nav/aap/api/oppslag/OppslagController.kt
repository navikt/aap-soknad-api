package no.nav.aap.api.oppslag

import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import no.nav.aap.api.felles.Kontonummer
import no.nav.aap.api.oppslag.OppslagController.Companion.OPPSLAG_BASE
import no.nav.aap.api.oppslag.arbeid.ArbeidClient
import no.nav.aap.api.oppslag.arbeid.Arbeidsforhold
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagClient
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.behandler.RegistrertBehandler
import no.nav.aap.api.oppslag.kontaktinformasjon.KRRClient
import no.nav.aap.api.oppslag.kontaktinformasjon.Kontaktinformasjon
import no.nav.aap.api.oppslag.konto.KontoClient
import no.nav.aap.api.oppslag.person.PDLClient
import no.nav.aap.api.oppslag.person.Søker
import no.nav.aap.api.oppslag.søknad.SøknadClient
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker.Companion.TIKA
import no.nav.aap.api.søknad.minside.MinSideRepository.MinSideBaseEntity.Companion.CREATED
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.spring.ProtectedRestController

@ProtectedRestController(value = [OPPSLAG_BASE], issuer = IDPORTEN)
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
    fun søker() = runBlocking {
        val s1 = async(Dispatchers.IO + RequestContextCoroutineContext()) { pdl.søkerMedBarn() }
        val s2 = async(Dispatchers.IO + RequestContextCoroutineContext()) { behandler.behandlerInfo() }
        val s3 = async(Dispatchers.IO + RequestContextCoroutineContext()) { arbeid.arbeidInfo() }
        val s4 = async(Dispatchers.IO + RequestContextCoroutineContext()) { krr.kontaktInfo() }
        val s5 = async(Dispatchers.IO + RequestContextCoroutineContext()) { konto.kontoInfo() }
        lookup(s1,s2,s3,s4,s5)
    }

    private suspend fun lookup(s1 : Deferred<Søker>,s2 : Deferred<List<RegistrertBehandler>>,  s3 : Deferred<List<Arbeidsforhold>>,
                               s4 : Deferred<Kontaktinformasjon?>, s5 : Deferred<Kontonummer?>) =
        coroutineScope {
            val res = awaitAll(s1,s2,s3,s4,s5)
            SøkerInfo(res[0] as Søker, res[1] as List<RegistrertBehandler>, res[2] as List<Arbeidsforhold>, res[3] as Kontaktinformasjon?,
                res[4] as Kontonummer?)
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
class RequestContextCoroutineContext(private val requestAttributes: RequestAttributes? = RequestContextHolder.getRequestAttributes()) : ThreadContextElement<RequestAttributes?> {
    val log = getLogger(javaClass)

    companion object Key : CoroutineContext.Key<RequestContextCoroutineContext>

    override val key: CoroutineContext.Key<RequestContextCoroutineContext> get() = Key

    override fun updateThreadContext(context: CoroutineContext): RequestAttributes? {
        log.trace("ASYNC update")
        val previousAttributes =  RequestContextHolder.getRequestAttributes()
        RequestContextHolder.setRequestAttributes(requestAttributes)
        return previousAttributes
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: RequestAttributes?) {
        log.trace("ASYNC restore")
        oldState?.let {
           RequestContextHolder.setRequestAttributes(oldState)
       } ?: RequestContextHolder.resetRequestAttributes()
    }
}