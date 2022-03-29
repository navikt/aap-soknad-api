package no.nav.aap.api.oppslag

import kotlinx.coroutines.async
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
    suspend fun søker() = coroutineScope  {
        val s = s()
        val two = two()
        val three = three()
        val four = four()
        SøkerInfo(s.await(), two.await(), three.await(), four.await())
    }

    suspend fun s() = coroutineScope { async(TokenValidationThreadContextElement(h)) { pdl.søkerMedBarn() } }
    suspend fun two() = coroutineScope { async(TokenValidationThreadContextElement(h)) { behandler.behandlere() } }
    suspend fun three() = coroutineScope { async(TokenValidationThreadContextElement(h)) { arbeid.arbeidsforhold() } }
    suspend fun four() = coroutineScope { async(TokenValidationThreadContextElement(h)) { krr.målform() } }

}