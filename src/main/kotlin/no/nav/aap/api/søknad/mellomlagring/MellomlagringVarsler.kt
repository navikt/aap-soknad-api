package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.net.InetAddress.*
import java.net.InetSocketAddress.*
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideRepositories
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient.Builder
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class MellomlagringVarsler(private val minside: MinSideClient, private val lager: Mellomlager, private val elector: LeaderElector, private val repos: MinSideRepositories) {
    val log = getLogger(javaClass)

    @Scheduled(fixedDelayString = "#{'\${buckets.mellom.purring.delay}'}", initialDelay = 10, timeUnit = SECONDS)
    fun sjekkVarsling() {
        with(lager.config().purring) {
            if (enabled && elector.erLeder(ME))  {
                log.info("Pod $ME: Ser etter snart utgåtte mellomlagringer ikke oppdatert på ${eldreEnn.toDays()} dager")
                try {
                    val gamle = lager.ikkeOppdatertSiden(eldreEnn).also {
                        log.info("Disse kan jo purres: $it")
                    }
                }
                catch (e: Exception) {
                    log.warn("OOPS",e)
                }
                /*
                gamle.forEach {
                    log.trace("Avslutter ${it.third} for ${it.first} siden opprettet er ${it.second}")
                    repos.beskjeder.findByFnrAndEventidAndDoneIsFalse(it.first.fnr,it.third)?.let { _ ->
                        log.trace("Avslutter gammel beskjed om mellomlagring og oppretter ny om snart utgått mellomlagring")
                        minside.avsluttBeskjed(it.first, it.third)
                        minside.opprettBeskjed(it.first,"Din mellomlagrede søknad fjernes snart",  UUID.randomUUID(),false, MINAAPSTD,true)
                    } ?: log.trace("Oppretter ingn beskjed om snart utgått mellomlagret søknad ")
                }*/
            }
            else {
                log.trace("Pod $ME. Ingen sjekk av snart utgåtte mellomlagringer")
            }
        }
    }

    companion object {
          val ME = getLocalHost().hostName
    }
}


@Component
class LeaderElector(@Value("\${elector.path}") private val elector: String, private val b: Builder) {
    val log = getLogger(javaClass)

    fun erLeder(me: String) =
        b.baseUrl("http://$elector").build()
            .get()
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<Leader>()
            .doOnError { t: Throwable -> log.warn("Leader oppslag mot $elector feilet", t) }
            .doOnSuccess { log.trace("Leader er ${it.name}, jeg er $me") }
            .block()?.name == me

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Leader(val name: String)
}