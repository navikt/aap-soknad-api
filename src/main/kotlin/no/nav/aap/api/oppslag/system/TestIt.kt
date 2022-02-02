package no.nav.aap.api.oppslag.system

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI


@ConfigurationProperties(prefix = "sts")
@ConstructorBinding
class TestIt (val username: String, val password: String, @DefaultValue("true") val enabled: Boolean) : AbstractRestConfig(URI("http://www.vg.no"), "ja", enabled)