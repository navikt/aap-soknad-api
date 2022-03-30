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
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.spring.ProtectedRestController
import org.slf4j.MDC
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import reactor.util.function.Tuple3
import java.util.concurrent.ScheduledExecutorService

import java.util.function.BiFunction





@ProtectedRestController(value = ["/oppslag"], issuer = Constants.IDPORTEN)
class OppslagController(val pdl: PDLClient,
                        val behandler: BehandlerClient,
                        val arbeid: ArbeidsforholdClient,
                        val krr: KRRClient,
                        val h: TokenValidationContextHolder) {
    init {
         val decorator1 = BiFunction<Scheduler, ScheduledExecutorService, ScheduledExecutorService?> { _: Scheduler, serv: ScheduledExecutorService? ->
            log.info("ZIP Decorating $h")
            serv
        }
        Schedulers.addExecutorServiceDecorator("test",decorator1)
    }

    @GetMapping("/soeker")
     fun søker() :SøkerInfo {
        try {
        log.info("ZIP start")
        Mono.zip(
                krr.kontaktinfoM(),
                behandler.behandlereM(),
                arbeid.arbeidsforholdM()).map(this::combine).block()
        log.info("ZIP end")
        }
        catch (e: Exception) {
            log.warn("ZIP oops",e)
        }
        log.info("SYNC start")
        return SøkerInfo(pdl.søkerMedBarn(),behandler.behandlere(),arbeid.arbeidsforhold(),krr.kontaktinfo())
            .also { log.info("SYNC end") }
    }
    private fun combine(zip: Tuple3<KontaktinformasjonDTO, List<BehandlerDTO>,List<ArbeidsforholdDTO>>) {
        log.info("ZIP ${zip.t1} and ${zip.t2} and ${zip.t3}")
    }
    companion object {
        val log = LoggerUtil.getLogger(OppslagController::class.java)

        init {
            log.info("ZIP hook init")
            Schedulers.onScheduleHook("mdc") { runnable: Runnable ->
                log.info("ZIP hook in action")
                val map = MDC.getCopyOfContextMap()
                Runnable {
                    if (map != null) {
                        MDC.setContextMap(map)
                    }
                    try {
                        log.info("ZIP hook run")
                        runnable.run()
                    } finally {
                        log.info("ZIP hook done")
                        MDC.clear()
                    }
                }
            }
        }
    }
}