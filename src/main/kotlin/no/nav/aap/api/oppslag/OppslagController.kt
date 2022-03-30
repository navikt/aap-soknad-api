package no.nav.aap.api.oppslag

import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdClient
import no.nav.aap.api.oppslag.arbeidsforhold.ArbeidsforholdDTO
import no.nav.aap.api.oppslag.behandler.BehandlerClient
import no.nav.aap.api.oppslag.behandler.BehandlerDTO
import no.nav.aap.api.oppslag.krr.KRRClient
import no.nav.aap.api.oppslag.krr.KontaktinformasjonDTO
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.util.Constants
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono
import reactor.util.function.Tuple3

@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLClient,
                        val behandler: BehandlerClient,
                        val arbeid: ArbeidsforholdClient,
                        val krr: KRRClient) {

    private val log = LoggerUtil.getLogger(javaClass)
    @GetMapping("/soeker")
     fun søker() :SøkerInfo {
        log.info("ZIP start")
         Mono.zip(
                krr.kontaktinfoM(),
                behandler.behandlereM(),
                arbeid.arbeidsforholdM())
            .map(this::combine);
        log.info("ZIP end")

        return SøkerInfo(pdl.søkerMedBarn(),behandler.behandlere(),arbeid.arbeidsforhold(),krr.kontaktinfo())
    }
    private fun combine(zip: Tuple3<KontaktinformasjonDTO, List<BehandlerDTO>,List<ArbeidsforholdDTO>>) {
           log.info("ZIP ${zip.t1} and ${zip.t2} and ${zip.t3}")
    }
}