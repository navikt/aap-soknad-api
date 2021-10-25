package no.nav.aap.api.rest.sts

import com.nimbusds.oauth2.sdk.GrantType.CLIENT_CREDENTIALS
import no.nav.aap.api.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters.FormInserter
import org.springframework.web.reactive.function.BodyInserters.fromFormData
import org.springframework.web.util.UriBuilder
import java.net.URI
import java.time.Duration


@ConfigurationProperties(prefix = "sts")
class STSConfig @ConstructorBinding constructor(
    @DefaultValue(DEFAULT_BASE_URI) baseUri: URI,
    @DefaultValue(DEFAULT_SLACK) internal val slack: Duration,  val username: String,  val password: String,
    @DefaultValue(PING_PATH) pingPath: String, @DefaultValue("true") enabled: Boolean,
    @DefaultValue(DEFAULT_PATH) val stsPath: String) : AbstractRestConfig(baseUri, pingPath, enabled) {

     fun stsURI(b: UriBuilder): URI {
        return b.path(stsPath)
            .build()
    }

    fun body(): FormInserter<String> {
        val m: LinkedMultiValueMap<String, String> = LinkedMultiValueMap<String, String>()
        m.add(GRANT_TYPE, CLIENT_CREDENTIALS.value)
        m.add(SCOPE, "openid")
        return fromFormData(m)
    }

    override fun toString() = "${javaClass.simpleName} [[username=$username,slack=$slack,service=$stsPath]"

    companion object {
        private const val DEFAULT_BASE_URI = "http://must.be.set"
        private const val GRANT_TYPE = "grant_type"
        private const val DEFAULT_PATH = "/rest/v1/sts/token"
        private const val DEFAULT_SLACK = "20s"
        private const val PING_PATH = ".well-known/openid-configuration"
        private const val SCOPE = "scope"
    }
}