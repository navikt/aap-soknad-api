package no.nav.aap.api.rest

import no.nav.aap.api.util.URIUtil
import java.net.URI

 abstract class AbstractRestConfig(val baseUri: URI, protected val pingPath: String, val isEnabled: Boolean) {
    fun pingEndpoint() = URIUtil.uri(baseUri, pingPath)
    fun name()  = baseUri.host
     override fun toString() = "${javaClass.simpleName} [pingPath=$pingPath,enabled=$isEnabled,baseUri=$baseUri]"
 }