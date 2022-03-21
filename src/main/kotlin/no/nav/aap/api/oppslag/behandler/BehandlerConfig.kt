package no.nav.aap.api.oppslag.behandler

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties("behandler")
@ConstructorBinding
class BehandlerConfig(
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue(DEFAULT_PATH) val path: String,
        @DefaultValue("true")  enabled: Boolean,
        baseUri: URI) : AbstractRestConfig(baseUri, pingPath, enabled) {


    companion object {
        const val BEHANDLER = "behandler"
        const val DEFAULT_PATH = "api/person/v1/behandler/self"
        const val DEFAULT_PING_PATH = "is_alive"
    }
}