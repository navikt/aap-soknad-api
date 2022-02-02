package no.nav.aap.api.oppslag.system

import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.net.URI
import java.time.Duration


@ConfigurationProperties(prefix = "sts")
@ConstructorBinding
class TestIt(@DefaultValue(DEFAULT_SLACK) val slack: Duration,
              val base: URI,
             val username: String,
             val password: String,
             @DefaultValue("true") val enabled: Boolean,
             @DefaultValue(PING_PATH) val ping: String,
             @DefaultValue(DEFAULT_PATH) private val stsPath:  String) : AbstractRestConfig(base, ping, enabled)