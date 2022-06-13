package no.nav.aap.api.oppslag

import no.nav.aap.api.oppslag.arbeid.ArbeidClient
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.krr.KRRClient
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.oppslag.saf.SafClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.model.SøkerInfo
import no.nav.aap.joark.DokumentInfoId
import no.nav.aap.util.Constants
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.CacheControl.noCache
import org.springframework.http.ContentDisposition.attachment
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.time.Duration

@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLClient,
                        val behandler: BehandlerClient,
                        val arbeid: ArbeidClient,
                        val krr: KRRClient,
                        val saf: SafClient,
                        val dittNav: DittNavClient) {

    val log = LoggerUtil.getLogger(javaClass)

    @GetMapping("/soeker")
    fun søker() = SøkerInfo(
            pdl.søkerMedBarn(),
            behandler.behandlere(),
            arbeid.arbeidsforhold(),
            krr.kontaktinfo())
        .also {
            val b = dittNav.opprettetMellomlagringBeskjed()
            log.trace("Beskjed $b")
            val uuid = dittNav.opprettBeskjed(tekst = "Du har en påbegynt søknad", varighet = Duration.ofDays(1))
            log.trace("Søker er $it")
        }

    @GetMapping("/saf")
    fun dokument(@PathVariable journalpostId: String, @PathVariable dokumentInfoId: DokumentInfoId) =
        saf.dokument(journalpostId, dokumentInfoId)
            ?.let {
                ok()
                    // .contentType(MediaType.parseMediaType(it.contentType))
                    .cacheControl(noCache().mustRevalidate())
                    .headers(HttpHeaders().apply {
                        contentDisposition = attachment()
                            //    .filename(it.metadata[Dokumentlager.FILNAVN]!!)
                            .build()
                    })
                    .body(it)
            } ?: notFound().build()
}