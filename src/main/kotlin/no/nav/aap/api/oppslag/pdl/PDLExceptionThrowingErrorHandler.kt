package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.api.util.LoggerUtil.getLogger
import no.nav.aap.api.util.LoggerUtil.getSecureLogger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException.create
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.HttpStatusCodeException
import java.nio.charset.Charset.defaultCharset

@Component
internal class PDLExceptionThrowingErrorHandler : PDLErrorHandler {
    private val log = getLogger(javaClass)
    private val secureLogger = getSecureLogger()

    override fun <T> handleError(e: GraphQLErrorsException): T {
        log.warn("PDL feilet, se secure logs for mer detaljer")
        secureLogger.error("PDL oppslag returnerte ${e.errors.size} feil. ${e.errors}", e)
        val errorMessage = e.message ?: "Ukjent feil"
        val firstExceptionCode = e.errors.firstOrNull()?.extensions?.get("code")

        throw if (firstExceptionCode != null) {
            exceptionFra(firstExceptionCode.toString(), errorMessage)
        } else {
            HttpServerErrorException(INTERNAL_SERVER_ERROR, errorMessage, null, null, null)
        }
    }

    companion object {
        private const val UAUTENTISERT = "unauthenticated"
        private const val FORBUDT = "unauthorized"
        private const val UGYLDIG = "bad_request"
        private const val IKKEFUNNET = "not_found"
        private fun exceptionFra(kode: String, msg: String): HttpStatusCodeException =
            when (kode) {
                UAUTENTISERT -> exception(UNAUTHORIZED, msg)
                FORBUDT -> exception(FORBIDDEN, msg)
                UGYLDIG -> exception(BAD_REQUEST, msg)
                IKKEFUNNET -> exception(NOT_FOUND, msg)
                else -> HttpServerErrorException(INTERNAL_SERVER_ERROR, msg)
            }

        private fun exception(status: HttpStatus, msg: String) =
            create(status, msg, HttpHeaders(), ByteArray(0), defaultCharset())
    }
}