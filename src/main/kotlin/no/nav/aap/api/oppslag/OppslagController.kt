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
        val zip =  Mono.zip(
                krr.kontaktinfoM(),
                behandler.behandlereM(),
                arbeid.arbeidsforholdM()).flatMap(data->{
            log.info(data.getT1());
            log.info(data.getT2());
            log.info(data.getT3());
        }
        log.info("ZIP end")
        log.info("SYNC start")
        return SøkerInfo(pdl.søkerMedBarn(),behandler.behandlere(),arbeid.arbeidsforhold(),krr.kontaktinfo())
            .also { log.info("SYNC end") }
    }
    private fun combine(zip: Tuple3<KontaktinformasjonDTO, List<BehandlerDTO>,List<ArbeidsforholdDTO>>) {
        log.info("COMBINE start")
        log.info("ZIP ${zip.t1} and ${zip.t2} and ${zip.t3}")
        log.info("COMBINE end")
    }
}