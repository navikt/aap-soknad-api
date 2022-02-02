package no.nav.aap.api.oppslag.system

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "sts")
@ConstructorBinding
class TestIt (val username: String, val password: String)