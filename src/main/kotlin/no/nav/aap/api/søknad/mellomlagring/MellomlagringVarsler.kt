package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.net.InetAddress
import java.net.InetSocketAddress.*
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPSTD
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient.Builder
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class MellomlagringVarsler(private val minside: MinSideClient, private val lager: Mellomlager, private val elector: LeaderElector) {
    val log = getLogger(javaClass)
    @Scheduled(fixedDelayString = "#{'\${buckets.mellom.purring.delay}'}", initialDelay = 10, timeUnit = SECONDS)
    fun sjekkVarsling() {
        with(lager.config().purring) {
            if (enabled)  {
                log.trace("Ser etter snart utgåtte mellomlagringer eldre enn ${alder.toHours()} og leader status ${elector.isLeaader()}")
                val gamle = lager.ikkeOppdatertSiden(alder)
                log.trace("Disse skal varsles:  $gamle")
                if (elector.isLeaader()) {
                    gamle.forEach {
                        log.trace("Avslutter ${it.third} for ${it.first} siden opprettet er ${it.second}")
                        minside.avsluttBeskjed(STANDARD,it.first,it.third)
                        minside.opprettBeskjed(it.first,"Dette er en purring", UUID.randomUUID(), MINAAPSTD,true)
                    }
                }
            }
            else {
                log.trace("Ingen sjekk av snart utgåtte mellomlagringer")
            }
        }
    }
}


@Component
class LeaderElector(@Value("\${elector.path}") private val elector: String, private val b: Builder) {
    val log = getLogger(javaClass)

    fun isLeaader() =
        b.baseUrl("http://$elector").build()
            .get()
            .accept(APPLICATION_JSON, parseMediaType("text/plain; charset=utf-8"))
            .retrieve()
            .bodyToMono<Leader>()
            .doOnError { t: Throwable ->
                log.warn("Leader oppslag feilet", t)
            }
            .doOnSuccess {
                log.trace("Leader er $it")
            }
            .block()?.name == (InetAddress.getLocalHost().hostName
            ?: false)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Leader(val name: String)
}