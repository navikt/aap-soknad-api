package no.nav.aap.api.oppslag.krr

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import java.net.URI


@ConfigurationProperties(prefix = "krr")
@ConstructorBinding
class KRRConfig (@DefaultValue(DEFAULT_BASE_URI) baseUri: URI,
                 @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
                 @DefaultValue(DEFAULT_PERSON_PATH) private val personPath: String,
                 @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, enabled) {

    fun kontaktUri(b: UriBuilder) = b.path(personPath).build()

    companion object {
        const val KRR = "krr"
        private const val DEFAULT_PING_PATH = "rest/ping"
        private const val DEFAULT_PERSON_PATH = "rest/v1/person"
        private const val DEFAULT_BASE_URI = "https://digdir-krr-proxy.intern.nav.no"
    }
}