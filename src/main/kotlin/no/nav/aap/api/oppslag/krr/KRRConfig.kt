package no.nav.aap.api.oppslag.krr

import java.net.URI
import java.time.Duration
import no.nav.aap.api.oppslag.krr.KRRConfig.Companion.KRR
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder

@ConfigurationProperties(KRR)
@ConstructorBinding
class KRRConfig(@DefaultValue(DEFAULT_URI) baseUri: URI,
                @DefaultValue(PINGPATH) pingPath: String,
                @DefaultValue(DEFAULT_PERSON_PATH) private val personPath: String,
                @DefaultValue("3")  retries: Long,
                @DefaultValue("100ms")  delay: Duration,
                @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, KRR, enabled,retries,delay) {

    fun kontaktUri(b: UriBuilder) = b.path(personPath).build()

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri,  personPath=$personPath, pingEndpoint=$pingEndpoint]"

    companion object {
        const val KRR = "krr"
        private const val DEFAULT_URI = "http://digdir-krr-proxy.team-rocket"
        private const val PINGPATH = "internal/health/liveness"
        private const val DEFAULT_PERSON_PATH = "rest/v1/person"
    }
}