package no.nav.aap.api.oppslag.person

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import no.nav.aap.api.oppslag.person.PDLConfig.Companion.PDL
import no.nav.aap.rest.AbstractRestConfig

@ConfigurationProperties(PDL)
class PDLConfig(baseUri: URI,
                pingPath: String = DEFAULT_PING_PATH,
                @NestedConfigurationProperty private val retryCfg: RetryConfig = RetryConfig.DEFAULT,
                enabled: Boolean = true) : AbstractRestConfig(baseUri, pingPath, PDL, enabled,retryCfg) {

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        const val PDL_CREDENTIALS = "client-credentials-pdl"
        const val PDL = "pdl"
        private const val DEFAULT_PING_PATH = ""
    }
}