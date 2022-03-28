package no.nav.aap.api.oppslag

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdClient
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.krr.KRRClient
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.util.Constants
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping

@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLClient,
                        val behandler: BehandlerClient,
                        val arbeid: ArbeidsforholdClient,
                        val krr: KRRClient,
                       var h: TokenValidationContextHolder) {
    private val log = LoggerUtil.getLogger(javaClass)

    @GetMapping("/soeker")
    suspend fun søker() : SøkerInfo {
        var info =  SøkerInfo(pdl.søkerMedBarn(),behandler.behandlere(),arbeid.arbeidsforhold(),xxx() )
        /*
       try {
           log.info("COROUTINE KALL")
           val mf = xxx()
           log.info("COROUTINE KALL OK $mf")

       }
       catch (e: Exception) {
           log.info("COROUTINE KALL FEIL",e)
       }

         */
        return info;
    }

    suspend fun xxx() = coroutineScope {
        withContext(TokenValidationThreadContextElement(h)) { krr.målform() }
    }

}