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
class MellomlagringVarsler(private val minside: MinSideClient, private val elector: LeaderElector, private val repos: MinSideRepositories) {
    val log = getLogger(javaClass)

    @Scheduled(fixedDelayString = "#{'\${buckets.mellom.purring.delay}'}", initialDelay = 10, timeUnit = SECONDS)
    fun sjekkVarsling() {
         if (elector.erLeder()) {

         }
    }
}



@Component
class LeaderElector(@Value("\${elector.path}") private val elector: String, private val b: Builder) {
    val log = getLogger(javaClass)

    fun erLeder() =
        b.baseUrl("http://$elector").build()
            .get()
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono<Leader>()
            .doOnError { t: Throwable -> log.warn("Leader oppslag mot $elector feilet", t) }
            .doOnSuccess { log.trace("Leader er $it, jeg er $ME") }
            .block()?.name == ME

    companion object {
        val ME = getLocalHost().hostName
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Leader(val name: String)
}