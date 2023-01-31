package no.nav.aap.api.oppslag.konto

import java.net.URI
import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractRestConfig.RetryConfig.Companion.DEFAULT
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder

@ConfigurationProperties(KONTO)
class KontoConfig(@DefaultValue(DEFAULT_URI) baseUri: URI,
                  @DefaultValue(PINGPATH) pingPath: String,
                  @DefaultValue(DEFAULT_KONTO_PATH) private val kontoPath: String,
                  @NestedConfigurationProperty private val retryCfg: RetryConfig = DEFAULT,
                  @DefaultValue("false") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, KONTO, enabled,retryCfg) {

    fun kontoUri(b: UriBuilder) = b.path(kontoPath).build()

    override fun toString() = "$javaClass.simpleName [baseUri=$baseUri,  kontoPath=$kontoPath, pingEndpoint=$pingEndpoint]"

    companion object {
        const val KONTO = "konto"
        private const val DEFAULT_URI = "http://sokos-kontoregister-person.okonomi"
        private const val PINGPATH = "internal/is_alive"
        private const val DEFAULT_KONTO_PATH = "api/borger/v1/hent-aktiv-konto"
    }
}