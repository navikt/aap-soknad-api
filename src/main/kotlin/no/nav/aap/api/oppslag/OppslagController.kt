package no.nav.aap.api.oppslag

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdClient
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.krr.KRRClient
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.util.Constants
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping
import kotlinx.coroutines.*

@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLClient,
                        val behandler: BehandlerClient,
                        val arbeid: ArbeidsforholdClient,
                        val krr: KRRClient) {

    @GetMapping("/soeker")
    suspend fun søker() = coroutineScope {
        SøkerInfo(
                async { pdl.søkerMedBarn() }.await(),
                async { behandler.behandlere() }.await(),
                async { arbeid.arbeidsforhold() }.await(),
                async { krr.målform() }.await())
    }

}