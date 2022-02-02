package no.nav.aap.api.oppslag.pdl

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI
const val DEFAULT_BASE_URI = "http://pdl-api.pdl/graphql" // må settes så lenge pdl er on prem og vi i gcp
const val DEFAULT_PING_PATH = "/"
@ConfigurationProperties(prefix = "pdl")
@ConstructorBinding
class PDLConfig(
        @DefaultValue(DEFAULT_PING_PATH) pingPath: String,
        @DefaultValue("true") enabled: Boolean,
        @DefaultValue(DEFAULT_BASE_URI) baseUri: URI) : AbstractRestConfig(baseUri, pingPath, enabled)