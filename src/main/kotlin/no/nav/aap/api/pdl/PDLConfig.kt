package no.nav.aap.api.pdl

import no.nav.aap.api.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI

@ConfigurationProperties(prefix = "pdl")
class PDLConfig @ConstructorBinding constructor(
    @DefaultValue(DEFAULT_PING_PATH)  pingPath: String,
    @DefaultValue("true") enabled: Boolean,
    @DefaultValue(DEFAULT_BASE_URI) baseUri: URI) : AbstractRestConfig(baseUri, pingPath, enabled) {

    override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"

    companion object {
        private const val DEFAULT_BASE_URI = "http://pdl-api.pdl/graphql" // må settes så lenge pdl er on prem of vi i gcp
        private const val DEFAULT_PING_PATH = "/"
    }
}