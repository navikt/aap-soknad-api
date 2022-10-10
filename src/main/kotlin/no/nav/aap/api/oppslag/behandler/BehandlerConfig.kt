package no.nav.aap.api.oppslag.behandler

import java.net.URI
import no.nav.aap.api.oppslag.behandler.BehandlerConfig.Companion.BEHANDLER
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder

@ConfigurationProperties(BEHANDLER)
@ConstructorBinding
class BehandlerConfig(
        @DefaultValue(DEFAULT_URI) baseUri: URI,
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue(DEFAULT_PATH) private val path: String,
        @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, BEHANDLER, enabled) {

    fun path(b: UriBuilder) = b.path(path).build()

    companion object {
        const val BEHANDLER = "behandler"
        const val BEHANDLERPING = "${BEHANDLER}ping"
        const val DEFAULT_URI = "http://isdialogmelding.teamsykefravr"
        const val DEFAULT_PATH = "api/person/v1/behandler/self"
        const val DEFAULT_PING_PATH = "is_alive"
    }

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri,  path=$path, pingEndpoint=$pingEndpoint]"

}