package no.nav.aap.api.søknad.virus

import java.net.URI
import java.time.Duration
import no.nav.aap.api.søknad.virus.VirusScanConfig.Companion.VIRUS
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(VIRUS)
@ConstructorBinding
class VirusScanConfig(@DefaultValue(BASE_URI)  uri: URI,
                      @DefaultValue("3") retries: Long,
                      @DefaultValue("100ms")  delay: Duration,
                      @DefaultValue("true")  enabled: Boolean) : AbstractRestConfig(uri, "", VIRUS, enabled,retries,delay) {

    companion object {
        const val VIRUS = "virus"
        private const val BASE_URI = "http://clamav.clamav.svc.cluster.local/scan"
    }
}