package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.util.LoggerUtil
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException.create
import org.springframework.web.client.HttpStatusCodeException
import java.nio.charset.Charset.defaultCharset

@Component
internal class PDLExceptionThrowingErrorHandler : PDLErrorHandler {
    private val log = LoggerUtil.getLogger(javaClass)
    private val secureLogger = LoggerUtil.getSecureLogger()

    override fun <T> handleError(e: GraphQLErrorsException): T {
        log.warn("PDL feilet, se secure logs for mer detaljer")
        secureLogger.error("PDL oppslag returnerte ${e.errors.size} feil. ${e.errors}", e)
        throw exceptionFra(e.errors.firstOrNull()?.extensions?.get("code"), e.message ?: "Ukjent feil")
    }

    companion object {
        private const val UAUTENTISERT = "unauthenticated"
        private const val FORBUDT = "unauthorized"
        private const val UGYLDIG = "bad_request"
        private const val IKKEFUNNET = "not_found"
        private fun exceptionFra(kode: Any?, msg: String): HttpStatusCodeException =
            when (kode?.toString()) {
                UAUTENTISERT -> exception(UNAUTHORIZED, msg)
                FORBUDT -> exception(FORBIDDEN, msg)
                UGYLDIG -> exception(BAD_REQUEST, msg)
                IKKEFUNNET -> exception(NOT_FOUND, msg)
                else -> exception(INTERNAL_SERVER_ERROR, msg)
            }

        private fun exception(status: HttpStatus, msg: String) =
            create(status, msg, HttpHeaders(), ByteArray(0), defaultCharset())
    }
}