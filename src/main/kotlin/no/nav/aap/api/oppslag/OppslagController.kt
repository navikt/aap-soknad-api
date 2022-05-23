package no.nav.aap.api.oppslag

import no.nav.aap.api.oppslag.arbeid.ArbeidClient
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.krr.KRRClient
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.oppslag.saf.SafClient
import no.nav.aap.api.søknad.model.SøkerInfo
import no.nav.aap.util.Constants
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.CacheControl
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLClient,
                        val behandler: BehandlerClient,
                        val arbeid: ArbeidClient,
                        val krr: KRRClient,
                        val saf: SafClient) {

    val log = LoggerUtil.getLogger(javaClass)

    @GetMapping("/soeker")
    fun søker() = SøkerInfo(
            pdl.søkerMedBarn(),
            behandler.behandlere(),
            arbeid.arbeidsforhold(),
            krr.kontaktinfo())
        .also { log.trace("Søker er $it") }

    @GetMapping("/saf")
    fun dokument(@PathVariable journalpostId: String, @PathVariable dokumentInfoId: String) =
        saf.dokument(journalpostId, dokumentInfoId)
            ?.let {
                ResponseEntity.ok()
                    // .contentType(MediaType.parseMediaType(it.contentType))
                    .cacheControl(CacheControl.noCache().mustRevalidate())
                    .headers(HttpHeaders().apply {
                        contentDisposition = ContentDisposition.attachment()
                            //    .filename(it.metadata[Dokumentlager.FILNAVN]!!)
                            .build()
                    })
                    .body(it)
            } ?: ResponseEntity.notFound().build()
}