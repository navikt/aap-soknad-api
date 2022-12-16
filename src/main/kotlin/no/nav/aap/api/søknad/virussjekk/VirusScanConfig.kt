package no.nav.aap.api.søknad.virussjekk

import java.net.URI
import no.nav.aap.api.søknad.virussjekk.VirusScanConfig.Companion.VIRUS
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(VIRUS)
@ConstructorBinding
class VirusScanConfig(@DefaultValue(BASE_URI)  uri: URI,
                      @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
                      @DefaultValue("true")  enabled: Boolean) : AbstractRestConfig(uri, "", VIRUS, enabled,retryCfg) {

    companion object {
        const val VIRUS = "virus"
        private const val BASE_URI = "http://clamav.clamav.svc.cluster.local/scan"
    }
}