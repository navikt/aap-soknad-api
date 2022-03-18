package no.nav.aap.api.oppslag

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdClient
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.krr.KRRClient
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.util.Constants
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping

@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLClient, val behandler: BehandlerClient, val arbeid: ArbeidsforholdClient, val krr: KRRClient) {

    private val log = LoggerUtil.getLogger(javaClass)

    @GetMapping("/soeker")
    fun søker() : SøkerInfo {
        log.info("Slår opp arbeidsforhold")
        val arbeidsforhold = arbeid.arbeidsforhold()
        log.info("Arbeidsforhold $arbeidsforhold")

        log.info("Slår opp behandlere")
        val behandlere = behandler.behandlere()
        log.info("Slått opp behandlere $behandlere")

        log.info("Slår opp målform")
        val mf = krr.målform()
        log.info("Målform $mf")
        return SøkerInfo(pdl.søkerMedBarn(), behandlere, arbeidsforhold)
    }
}