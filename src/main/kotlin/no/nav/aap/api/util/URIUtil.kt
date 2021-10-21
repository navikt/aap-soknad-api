package no.nav.aap.api.util

import org.springframework.http.HttpHeaders
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

object URIUtil {
    fun uri(base: String, path: String): URI {
        return uri(URI.create(base), path)
    }

    fun uri(base: URI, path: String, queryParams: HttpHeaders? = null): URI {
        return builder(base, path, queryParams)
            .build()
            .toUri()
    }

    private fun builder(base: URI, path: String, queryParams: HttpHeaders?): UriComponentsBuilder {
        return UriComponentsBuilder
            .fromUri(base)
            .pathSegment(path)
            .queryParams(queryParams)
    }

    fun queryParams(key: String, value: String): HttpHeaders {
        val queryParams = HttpHeaders()
        queryParams.add(key, value)
        return queryParams
    }

    fun queryParams(key: String, value: String, key1: String, value1: String): HttpHeaders {
        val queryParams = HttpHeaders()
        queryParams.add(key, value)
        queryParams.add(key1, value1)
        return queryParams
    }

    fun uri(base: URI, queryParams: HttpHeaders): URI {
        return uri(base, "/", queryParams)
    }
}