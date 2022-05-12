package no.nav.aap.api.oppslag.pdl

import no.nav.aap.api.oppslag.pdl.PDLConfig.Companion.PDL
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties(PDL)
@ConstructorBinding
class PDLConfig(baseUri: URI,
                @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
                @DefaultValue("true") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, PDL, enabled) {

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri, pingEndpoint=$pingEndpoint]"

    companion object {
        const val PDL_CREDENTIALS = "client-credentials-pdl"
        const val PDL = "pdl"
        const val DEFAULT_PING_PATH = ""
    }
}