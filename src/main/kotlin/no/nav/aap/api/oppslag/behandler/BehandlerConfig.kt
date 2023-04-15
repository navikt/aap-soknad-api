package no.nav.aap.api.oppslag.behandler

import java.net.URI
import java.time.Duration.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.web.util.UriBuilder
import reactor.util.retry.Retry.*
import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLER
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT

@ConfigurationProperties(BEHANDLER)
class BehandlerConfig(baseUri: URI = DEFAULT_URI, pingPath: String = DEFAULT_PING_PATH, val path: String = DEFAULT_PATH,
        @NestedConfigurationProperty val retryCfg: RetryConfig = DEFAULT,
        enabled: Boolean= true) : AbstractRestConfig(baseUri, pingPath, BEHANDLER, enabled,retryCfg) {
    fun path(b: UriBuilder) = b.path(path).build()


    companion object {
        const val BEHANDLER = "behandler"
        const val BEHANDLERPING = "${BEHANDLER}ping"
        private val DEFAULT_URI = URI.create("http://isdialogmelding.teamsykefravr")
        private const val DEFAULT_PATH = "api/person/v1/behandler/self"
        private const val DEFAULT_PING_PATH = "is_alive"
    }

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri,  path=$path, pingEndpoint=$pingEndpoint]"

}