package no.nav.aap.api.oppslag

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdClient
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.krr.KRRClient
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.util.Constants
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping

@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLClient,
                        val behandler: BehandlerClient,
                        val arbeid: ArbeidsforholdClient,
                        val krr: KRRClient,
                       var h: TokenValidationContextHolder) {

    @GetMapping("/soeker")
    suspend fun søker() = oppslag()
    
    suspend fun oppslag() = coroutineScope {
        withContext(TokenValidationThreadContextElement(h)) {
            SøkerInfo(pdl.søkerMedBarn(), behandler.behandlere(), arbeid.arbeidsforhold(), krr.målform())
        }
    }

}