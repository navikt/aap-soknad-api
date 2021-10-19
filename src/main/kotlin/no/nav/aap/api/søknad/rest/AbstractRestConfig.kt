package no.nav.aap.api.søknad.rest

import no.nav.aap.api.søknad.util.URIUtil
import java.net.URI

open class AbstractRestConfig protected constructor(
    val baseUri: URI,
    protected val pingPath: String,
    val isEnabled: Boolean
) {
    fun pingEndpoint(): URI {
        return URIUtil.uri(baseUri, pingPath)
    }

    fun name(): String {
        return baseUri.host
    }

    companion object {
        private const val IDPORTEN = "idporten"
        const val ISSUER = IDPORTEN
    }
}