package no.nav.aap.api.søknad.mellomlagring

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.net.InetAddress.*
import java.net.InetSocketAddress.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideRepositories
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.http.MediaType.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient.Builder
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class MellomlagringVarsler(private val minside: MinSideClient, private val elector: LeaderElector, private val repos: MinSideRepositories) : EnvironmentAware {
    private lateinit var env: Environment
    val log = getLogger(javaClass)

    @Scheduled(fixedDelayString = "#{'\${buckets.mellom.purring.delay}'}", initialDelay = 10, timeUnit = SECONDS)
    fun sjekkVarsling() {
        env.getProperty("elector.path")?.let {
            val request = HttpRequest.newBuilder()
                .uri(URI("http://$it"))
                .GET()
                .build()
            val client  = HttpClient.newHttpClient()
            val res = client.send(request, BodyHandlers.ofString())
            log.trace("Elector response $res")
    }
}

    override fun setEnvironment(env: Environment) {
       this.env = env
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