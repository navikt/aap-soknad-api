package no.nav.aap.api.util

import org.springframework.http.HttpHeaders
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

object URIUtil {
    fun uri(base: String, path: String) = uri(URI.create(base), path)
    fun uri(base: URI, path: String, queryParams: HttpHeaders? = null) =
        builder(base, path, queryParams).build().toUri()

    private fun builder(base: URI, path: String, queryParams: HttpHeaders?): UriComponentsBuilder =
        UriComponentsBuilder.fromUri(base).pathSegment(path).queryParams(queryParams)

    fun uri(base: URI, queryParams: HttpHeaders) = uri(base, "/", queryParams)
}