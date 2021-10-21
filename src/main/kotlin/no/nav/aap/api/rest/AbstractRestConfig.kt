package no.nav.aap.api.rest

import no.nav.aap.api.util.URIUtil
import java.net.URI

open class AbstractRestConfig protected constructor(val baseUri: URI, protected val pingPath: String, val isEnabled: Boolean) {
    fun pingEndpoint() = URIUtil.uri(baseUri, pingPath)
    fun name(): String = baseUri.host
}