package no.nav.aap.api.oppslag

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdClient
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.util.Constants
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping

@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLClient, val fastlege: BehandlerClient, val arbeid: ArbeidsforholdClient) {

    private val log = LoggerUtil.getLogger(javaClass)

    @GetMapping("/soeker")
    fun søker() : SøkerInfo {
        log.info("Arbeid")
        val a = arbeid.arbeidsforhold()
        log.info("Arbeid $a")
        log.info("Slår opp fastlege")
        val lege = fastlege.fastlege()
        log.info("Slått opp fastlege $lege")
        return SøkerInfo(pdl.søker(true), lege.firstOrNull())
    }
}