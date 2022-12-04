package no.nav.aap.api.oppslag.pdl

import java.net.URI
import java.time.Duration
import no.nav.aap.api.oppslag.pdl.PDLConfig.Companion.PDL
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(PDL)
@ConstructorBinding
class PDLConfig(baseUri: URI,
                @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
                @DefaultValue("3")  retries: Long,
                @DefaultValue("100ms")  delay: Duration,
                @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, PDL, enabled,retries,delay) {

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        const val PDL_CREDENTIALS = "client-credentials-pdl"
        const val PDL = "pdl"
        const val DEFAULT_PING_PATH = ""
    }
}