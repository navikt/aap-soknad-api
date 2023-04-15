package no.nav.aap.api.oppslag.kontaktinformasjon

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.web.util.UriBuilder
import no.nav.aap.api.oppslag.kontaktinformasjon.KRRConfig.Companion.KRR
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT

@ConfigurationProperties(KRR)
class KRRConfig(baseUri: URI = DEFAULT_URI, pingPath: String = PINGPATH, private val personPath: String = DEFAULT_PERSON_PATH,
                @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
                enabled: Boolean = true) : AbstractRestConfig(baseUri, pingPath, KRR, enabled,retryCfg) {

    fun kontaktUri(b: UriBuilder) = b.path(personPath).build()

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri,  personPath=$personPath, pingEndpoint=$pingEndpoint]"

    companion object {
        const val KRR = "krr"
        private val DEFAULT_URI = URI.create("http://digdir-krr-proxy.team-rocket")
        private const val PINGPATH = "internal/health/liveness"
        private const val DEFAULT_PERSON_PATH = "rest/v1/person"
    }
}