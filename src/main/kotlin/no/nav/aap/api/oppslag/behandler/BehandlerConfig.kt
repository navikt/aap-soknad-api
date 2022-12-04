package no.nav.aap.api.oppslag.behandler

import java.net.URI
import java.time.Duration
import java.time.Duration.*
import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLER
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.boot.convert.DurationStyle.*
import org.springframework.web.util.UriBuilder
import reactor.util.retry.Retry.*

@ConfigurationProperties(BEHANDLER)
@ConstructorBinding
class BehandlerConfig(
        @DefaultValue(DEFAULT_URI) baseUri: URI,
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String = DEFAULT_PING_PATH,
        @DefaultValue(DEFAULT_PATH) private val path: String = DEFAULT_PATH,
        @NestedConfigurationProperty val retry: RetryConfig = DEFAULT_RETRY,
        @DefaultValue("true") enabled: Boolean = true) : AbstractRestConfig(baseUri, pingPath, BEHANDLER, enabled,retry.retries,retry.delayed) {
    fun path(b: UriBuilder) = b.path(path).build()

    data class RetryConfig(@DefaultValue(DEFAULT_RETRIES)  val retries: Long,
                           @DefaultValue(DEFAULT_DELAY)  val delayed: Duration) {
        companion object {
            const val DEFAULT_RETRIES = "3"
            const val DEFAULT_DELAY = "133ms"
            val DEFAULT = RetryConfig(DEFAULT_RETRIES.toLong(), detectAndParse(DEFAULT_DELAY))
        }
    }

    companion object {
        val DEFAULT_RETRY = RetryConfig.DEFAULT
        const val BEHANDLER = "behandler"
        const val BEHANDLERPING = "${BEHANDLER}ping"
        const val DEFAULT_URI = "http://isdialogmelding.teamsykefravr"
        const val DEFAULT_PATH = "api/person/v1/behandler/self"
        const val DEFAULT_PING_PATH = "is_alive"
    }

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri,  path=$path, pingEndpoint=$pingEndpoint]"

}