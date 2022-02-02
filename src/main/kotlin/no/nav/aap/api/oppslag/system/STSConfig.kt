package no.nav.aap.api.oppslag.system

import com.nimbusds.oauth2.sdk.GrantType.CLIENT_CREDENTIALS
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.BodyInserters.FormInserter
import org.springframework.web.util.UriBuilder
import java.net.URI
import java.time.Duration


@ConfigurationProperties(prefix = "sts")
@ConstructorBinding
class STSConfig(@DefaultValue(DEFAULT_SLACK) val slack: Duration,
                private val base: URI,
                val username: String,
                val password: String,
                @DefaultValue("true") val enabled: Boolean,
                @DefaultValue(PING_PATH) val ping: String,
                @DefaultValue(DEFAULT_PATH) private val stsPath:  String) : AbstractRestConfig(base, ping, enabled){

    fun stsBody(): FormInserter<String> {
        val m = LinkedMultiValueMap<String, String>()
        m.add(GRANT_TYPE, CLIENT_CREDENTIALS.value)
        m.add(SCOPE, "openid")
        return BodyInserters.fromFormData(m)
    }
    fun stsURI(b: UriBuilder) = b.path(stsPath).build()

    companion object {
        const val  GRANT_TYPE = "grant_type"
        const val DEFAULT_PATH = "/rest/v1/sts/token"
        const val DEFAULT_SLACK = "20s"
        const val PING_PATH = ".well-known/openid-configuration"
        const val SCOPE = "scope"
    }
}