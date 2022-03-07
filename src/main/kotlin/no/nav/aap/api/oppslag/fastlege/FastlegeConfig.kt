package no.nav.aap.api.oppslag.fastlege

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties(prefix = "fastlege")
@ConstructorBinding
class FastlegeConfig(
        @DefaultValue(DEFAULT_PING_PATH) val pp: String,
        @DefaultValue(DEFAULT_PATH) val path: String,
        @DefaultValue("true")  enabled: Boolean,
        @DefaultValue(DEFAULT_BASE_URI) baseUri: URI) : AbstractRestConfig(baseUri, pp, enabled) {


    companion object {
        const val FASTLEGE = "fastlege"
        const val DEFAULT_BASE_URI = "https://isdialogmelding.dev.intern.nav.no/api"
        const val DEFAULT_PATH = "/person/v1/behandler/self"
        const val DEFAULT_PING_PATH = "internal/isAlive"
    }
}