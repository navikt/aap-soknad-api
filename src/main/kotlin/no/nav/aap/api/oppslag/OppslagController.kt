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
    suspend fun søker() =SøkerInfo(s(), two(), three(), four())


    suspend fun s() = coroutineScope { withContext(TokenValidationThreadContextElement(h)) { pdl.søkerMedBarn() } }
    suspend fun two() = coroutineScope { withContext(TokenValidationThreadContextElement(h)) { behandler.behandlere() } }
    suspend fun three() = coroutineScope { withContext(TokenValidationThreadContextElement(h)) { arbeid.arbeidsforhold() } }
    suspend fun four() = coroutineScope { withContext(TokenValidationThreadContextElement(h)) { krr.målform() } }

}