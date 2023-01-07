package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.api.oppslag.graphql.GraphQLErrorExtensions.RecoverableGraphQLResponse.UnhandledGraphQLResponse
import no.nav.aap.api.oppslag.graphql.GraphQLErrorExtensions.UnrecoverableGraphQLResponse.BadGraphQLResponse
import no.nav.aap.api.oppslag.graphql.GraphQLErrorExtensions.UnrecoverableGraphQLResponse.NotFoundGraphQLResponse
import no.nav.aap.api.oppslag.graphql.GraphQLErrorExtensions.UnrecoverableGraphQLResponse.UnauthenticatedGraphQLResponse
import no.nav.aap.api.oppslag.graphql.GraphQLErrorExtensions.UnrecoverableGraphQLResponse.UnautorizedGraphQLResponse
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.BadRequest
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.NotFound
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.Unauthenticated
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.Unauthorized
import no.nav.aap.util.LoggerUtil
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED

object GraphQLErrorExtensions {

    private val log = LoggerUtil.getLogger(javaClass)

     fun GraphQLErrorsException.oversett() = oversett(code(), message ?: "Ukjent feil").also {
         log.warn("GraphQL oppslag returnerte ${errors.size} feil. ${errors}", this)
         log.warn("GraphQL oversatte feilkode til ${it.javaClass.simpleName}",it)
     }

    private fun GraphQLErrorsException.code() = errors.firstOrNull()?.extensions?.get("code")?.toString()

    private fun oversett(kode: String?, msg: String) =
        when (kode) {
            Unauthorized -> UnautorizedGraphQLResponse(UNAUTHORIZED,msg)
            Unauthenticated -> UnauthenticatedGraphQLResponse(FORBIDDEN,msg)
            BadRequest -> BadGraphQLResponse(BAD_REQUEST, msg)
            NotFound -> NotFoundGraphQLResponse(NOT_FOUND, msg)
            else -> UnhandledGraphQLResponse(INTERNAL_SERVER_ERROR,msg)
        }
    abstract class UnrecoverableGraphQLResponse(status: HttpStatus, msg: String) : RuntimeException("${status.value()}-$msg", null) {
        class NotFoundGraphQLResponse(status: HttpStatus, msg: String) : UnrecoverableGraphQLResponse(status,msg)
        class BadGraphQLResponse(status: HttpStatus, msg: String) : UnrecoverableGraphQLResponse(status,msg)
        class UnauthenticatedGraphQLResponse(status: HttpStatus, msg: String) : UnrecoverableGraphQLResponse(status,msg)
        class UnautorizedGraphQLResponse(status: HttpStatus, msg: String) : UnrecoverableGraphQLResponse(status,msg)

    }
    abstract class RecoverableGraphQLResponse(status: HttpStatus, msg: String) : RuntimeException("${status.value()}-$msg", null) {
        class UnhandledGraphQLResponse(status: HttpStatus, msg: String) : RecoverableGraphQLResponse(status,msg)
    }
}