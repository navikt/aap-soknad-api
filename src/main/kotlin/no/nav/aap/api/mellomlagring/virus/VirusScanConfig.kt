package no.nav.aap.api.mellomlagring.virus

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties(prefix = "virus")
@ConstructorBinding
data class VirusScanConfig(@DefaultValue(BASE_URI) val uri: URI,
                      @DefaultValue(PATH) val path: String,
                      @DefaultValue("true") val enabled: Boolean) : AbstractRestConfig(uri, path,enabled) {

    companion object {
        private const val BASE_URI = "http://clamav.clamav.svc.cluster.local"
        private const val PATH = "scan"

    }
}