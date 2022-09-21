package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.LoggerUtil.getSecureLogger
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException.create
import java.nio.charset.Charset.defaultCharset

@Component
class GraphQLExceptionThrowingErrorHandler : GraphQLErrorHandler {
    private val log = getLogger(javaClass)
    private val secureLogger = getSecureLogger()

    override fun handle(e: GraphQLErrorsException) : Nothing {
        log.warn("GraphQL feilet, se secure logs for mer detaljer")
        secureLogger.error("GraphQL oppslag returnerte ${e.errors.size} feil. ${e.errors}", e)
        log.trace(CONFIDENTIAL,"GraphQL oppslag returnerte ${e.errors.size} feil. ${e.errors}", e)
        throw e.httpClientException()
    }

    private fun GraphQLErrorsException.code() = errors.firstOrNull()?.extensions?.get("code")?.toString()
    private fun GraphQLErrorsException.httpClientException() = exceptionFra(code(), message ?: "Ukjent feil")

    private fun exceptionFra(kode: String?, msg: String) =
        when (kode) {
            "unauthenticated" -> create(UNAUTHORIZED, msg)
            "unauthorized" -> create(FORBIDDEN, msg)
            "bad_request" -> create(BAD_REQUEST, msg)
            "not_found" -> create(NOT_FOUND, msg)
            else -> create(INTERNAL_SERVER_ERROR, msg)
        }

    private fun create(status: HttpStatus, msg: String) =
        create(status, msg, HttpHeaders(), ByteArray(0), defaultCharset())
}