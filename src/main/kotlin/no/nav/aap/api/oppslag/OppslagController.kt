package no.nav.aap.api.oppslag

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidClient
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.krr.KRRClient
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.model.SøkerInfo
import no.nav.aap.util.Constants
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping

@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLClient,
                        val behandler: BehandlerClient,
                        val arbeid: ArbeidClient,
                        val krr: KRRClient) {

    val log = LoggerUtil.getLogger(javaClass)

    @GetMapping("/soeker")
    fun søker() = SøkerInfo(
            pdl.søkerMedBarn(),
            behandler.behandlere(),
            arbeid.arbeidsforhold(),
            krr.kontaktinfo())
        .also { log.trace("Søker er $it") }
}