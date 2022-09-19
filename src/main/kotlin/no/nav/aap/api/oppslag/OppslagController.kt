package no.nav.aap.api.oppslag

import no.nav.aap.api.oppslag.OppslagController.Companion.OPPSLAG_BASE
import no.nav.aap.api.oppslag.arbeid.ArbeidClient
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.konto.KontoClient
import no.nav.aap.api.oppslag.krr.KRRClient
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.oppslag.arkiv.ArkivOppslagClient
import no.nav.aap.api.oppslag.søknad.SøknadClient
import no.nav.aap.api.søknad.model.SøkerInfo
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.SortDefault
import org.springframework.http.CacheControl.noCache
import org.springframework.http.ContentDisposition.attachment
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.*

@ProtectedRestController(value = [OPPSLAG_BASE], issuer = IDPORTEN)
class OppslagController(
    val pdl: PDLClient,
    val behandler: BehandlerClient,
    val arbeid: ArbeidClient,
    val krr: KRRClient,
    val søknad: SøknadClient,
    val konto: KontoClient,
    val arkiv: ArkivOppslagClient) {

    val log = getLogger(javaClass)

    @GetMapping("/soeker")
    fun søker() = SøkerInfo(
        pdl.søkerMedBarn(),
        behandler.behandlerInfo(),
        arbeid.arbeidInfo(),
        krr.kontaktInfo(),
        konto.kontoInfo()
    ).also {
        log.trace("Søker er $it")
        try {
            søknaderNy(PageRequest.of(0,100,DESC,"created"))  // TODO midlertidig test
        }
        catch (e: Exception){
            log.warn("OOPS",e)
        }
    }

    @GetMapping("/dokumenter")
    fun dokumenter() = arkiv.dokumenter()

    @GetMapping("/soeknader")
    fun søknader(@SortDefault(sort = ["created"], direction = DESC) @PageableDefault(size = 100) pageable: Pageable) =
        søknad.søknader(pageable)

    @GetMapping("/soeknaderNy")
    fun søknaderNy(@SortDefault(sort = ["created"], direction = DESC) @PageableDefault(size = 100) pageable: Pageable) =
        søknad.søknaderNy(pageable).also {
            it.forEachIndexed{ i,s -> log.trace("$i Ny  -> $s")}
        }
    @GetMapping("/soeknad/{uuid}")
    fun søknad(@PathVariable uuid: UUID) = søknad.søknad(uuid)

    @GetMapping("/soeknad/{journalpostId}")
    fun søknad(@PathVariable journalpostId: String): Nothing = TODO()

    @GetMapping(DOKUMENT)
    fun dokument(@PathVariable journalpostId: String, @PathVariable dokumentId: String) =
        arkiv.dokument(journalpostId, dokumentId)
            .let {
                ok()
                    .cacheControl(noCache().mustRevalidate())
                    .headers(HttpHeaders().apply {
                        contentDisposition = attachment()
                            .build()
                    })
                    .body(it)
            }

    companion object {
        const val OPPSLAG_BASE = "/oppslag"
        private const val DOKUMENT = "/dokument/{journalpostId}/{dokumentId}"
        const val DOKUMENT_PATH = "$OPPSLAG_BASE$DOKUMENT"
    }
}