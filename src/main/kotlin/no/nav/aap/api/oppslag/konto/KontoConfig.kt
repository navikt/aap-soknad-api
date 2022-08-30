package no.nav.aap.api.oppslag.konto

import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.rest.AbstractRestConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.web.util.UriBuilder
import java.net.URI

@ConfigurationProperties(KONTO)
@ConstructorBinding
class KontoConfig(@DefaultValue(DEFAULT_URI) baseUri: URI,
                  @DefaultValue(PINGPATH) pingPath: String,
                  @DefaultValue(DEFAULT_KONTO_PATH) private val kontoPath: String,
                  @DefaultValue("false") enabled: Boolean) : AbstractRestConfig(baseUri, pingPath, KONTO, enabled) {

    fun kontoUri(b: UriBuilder) = b.path(kontoPath).build()

    override fun toString() =
        "$javaClass.simpleName [baseUri=$baseUri,  kontoPath=$kontoPath, pingEndpoint=$pingEndpoint]"

    companion object {
        const val KONTO_CREDENTIALS = "client-credentials-sokos"
        const val KONTO = "konto"
        private const val DEFAULT_URI = "http://sokos-kontoregister-person.okonomi"
        private const val PINGPATH = "internal/is_alive"
        private const val DEFAULT_KONTO_PATH = "kontoregister/api/kontoregister/v1/hent-konto"
    }
}