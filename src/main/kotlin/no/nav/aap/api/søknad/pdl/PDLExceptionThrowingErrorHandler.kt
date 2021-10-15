package no.nav.aap.api.søknad.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLError
import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.api.søknad.util.StreamUtil.safeStream
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException.create
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.HttpStatusCodeException
import java.nio.charset.Charset
import java.util.*

@Component
class PDLExceptionThrowingErrorHandler : PDLErrorHandler {
    override fun <T> handleError(e: GraphQLErrorsException): T {
        LOG.warn("PDL oppslag returnerte {} feil. {}", e!!.errors.size, e.errors, e)
        throw safeStream(e.errors)
            .findFirst()
            .map { obj: GraphQLError -> obj.extensions }
            .map { m: Map<String, Any> -> m["code"] }
            .filter { obj: Any? -> Objects.nonNull(obj) }
            .map { obj: Any? -> String::class.java.cast(obj) }
            .map { k: String -> e.message?.let { exceptionFra(k, it) } }
            .orElse(e!!.message?.let { HttpServerErrorException(INTERNAL_SERVER_ERROR, it, null, null, null) })
    }

    companion object {
        private const val UAUTENTISERT = "unauthenticated"
        private const val FORBUDT = "unauthorized"
        private const val UGYLDIG = "bad_request"
        private const val IKKEFUNNET = "not_found"
        private val LOG = LoggerFactory.getLogger(PDLExceptionThrowingErrorHandler::class.java)
        private fun exceptionFra(kode: String, msg: String): HttpStatusCodeException {
            return when (kode) {
                UAUTENTISERT -> exception(
                    UNAUTHORIZED,
                    msg
                )
                FORBUDT -> exception(
                    FORBIDDEN,
                    msg
                )
                UGYLDIG -> exception(
                    BAD_REQUEST,
                    msg
                )
                IKKEFUNNET -> exception(
                    NOT_FOUND,
                    msg
                )
                else -> HttpServerErrorException(INTERNAL_SERVER_ERROR, msg)
            }
        }

        fun exception(status: HttpStatus, msg: String): HttpStatusCodeException {
            return create(status, msg, HttpHeaders(), ByteArray(0), Charset.defaultCharset())
        }
    }
}