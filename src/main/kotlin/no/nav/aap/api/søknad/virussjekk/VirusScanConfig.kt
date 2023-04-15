package no.nav.aap.api.søknad.virussjekk

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import no.nav.aap.api.søknad.virussjekk.VirusScanConfig.Companion.VIRUS
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT

@ConfigurationProperties(VIRUS)
class VirusScanConfig(uri: URI = BASE_URI,
                      @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
                      enabled: Boolean = true) : AbstractRestConfig(uri, "", VIRUS, enabled,retryCfg) {

    companion object {
        const val VIRUS = "virus"
        private val BASE_URI = URI.create("http://clamav.nais-system/scan")
    }
}