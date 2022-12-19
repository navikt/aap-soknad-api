package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import java.nio.charset.Charset
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.LoggerUtil.getSecureLogger
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class GraphQLDefaultErrorHandler : GraphQLErrorHandler {
    private val log = getLogger(javaClass)
    private val secureLogger = getSecureLogger()

    override fun handle(e: GraphQLErrorsException): Nothing {
        log.warn("GraphQL oppslag returnerte ${e.errors.size} feil. ${e.errors}", e)
        throw e.httpClientException().also { log.warn("GraphQL oversatte feilkode til ${it.javaClass.simpleName}",it) }
    }

    private fun GraphQLErrorsException.code() = errors.firstOrNull()?.extensions?.get("code")?.toString()
    private fun GraphQLErrorsException.httpClientException() = exceptionFra(code(), message ?: "Ukjent feil")

    private fun exceptionFra(kode: String?, msg: String) =
        when (kode) {
            "unauthorized", "unauthenticated" -> JwtTokenUnauthorizedException("$kode-$msg", null)
            "bad_request" -> GraphQLBad(BAD_REQUEST, msg)
            "not_found" -> GraphQLNotFound(NOT_FOUND, msg)
            else -> WebClientResponseException(INTERNAL_SERVER_ERROR.value(), msg, HttpHeaders(),null, Charset.defaultCharset(),null)
        }
    abstract class GraphQLUnreocoverableResponseException(status: HttpStatus, msg: String) : RuntimeException("${status.value()}-$msg", null)
    class GraphQLNotFound(status: HttpStatus, msg: String) : GraphQLUnreocoverableResponseException(status,msg)
    class GraphQLBad(status: HttpStatus, msg: String) : GraphQLUnreocoverableResponseException(status,msg)

}