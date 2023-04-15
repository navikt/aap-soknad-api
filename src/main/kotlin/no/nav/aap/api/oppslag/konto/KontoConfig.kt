package no.nav.aap.api.oppslag.konto

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.web.util.UriBuilder
import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT

@ConfigurationProperties(KONTO)
class KontoConfig(baseUri: URI = DEFAULT_URI, pingPath: String = PINGPATH,
                  private val kontoPath: String = DEFAULT_KONTO_PATH,
                  @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
                  enabled: Boolean = false) : AbstractRestConfig(baseUri, pingPath, KONTO, enabled,retryCfg) {

    fun kontoUri(b: UriBuilder) = b.path(kontoPath).build()

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri,  kontoPath=$kontoPath, pingEndpoint=$pingEndpoint]"

    companion object {
        const val KONTO = "konto"
        private val DEFAULT_URI = URI.create("http://sokos-kontoregister-person.okonomi")
        private const val PINGPATH = "internal/is_alive"
        private const val DEFAULT_KONTO_PATH = "api/borger/v1/hent-aktiv-konto"
    }
}