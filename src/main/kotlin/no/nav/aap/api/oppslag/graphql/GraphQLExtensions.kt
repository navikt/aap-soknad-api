package no.nav.aap.api.oppslag.graphql

import org.springframework.graphql.client.FieldAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import no.nav.aap.api.felles.error.IrrecoverableIntegrationException
import no.nav.aap.api.felles.error.RecoverableIntegrationException
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.BadRequest
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.NotFound
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.Unauthenticated
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.Unauthorized
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.IrrecoverableGraphQLException.BadGraphQLException
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.IrrecoverableGraphQLException.NotFoundGraphQLException
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.IrrecoverableGraphQLException.UnauthenticatedGraphQLException
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.IrrecoverableGraphQLException.UnauthorizedGraphQLException
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.RecoverableGraphQLException.UnhandledGraphQLException
import no.nav.aap.util.LoggerUtil

object GraphQLExtensions {

    private val log = LoggerUtil.getLogger(javaClass)

    const val IDENT = "ident"
    const val IDENTER = "identer"

    fun FieldAccessException.oversett() = oversett(response.errors.firstOrNull()?.extensions?.get("code")?.toString(), message ?: "Ukjent feil").also { e ->
        log.warn("GraphQL oppslag returnerte ${response.errors.size} feil. ${response.errors}, oversatte feilkode til ${e.javaClass.simpleName}",
            this)
    }

    private fun oversett(kode : String?, msg : String) =
        when (kode) {
            Unauthorized -> UnauthorizedGraphQLException(UNAUTHORIZED, msg)
            Unauthenticated -> UnauthenticatedGraphQLException(FORBIDDEN, msg)
            BadRequest -> BadGraphQLException(BAD_REQUEST, msg)
            NotFound -> NotFoundGraphQLException(NOT_FOUND, msg)
            else -> UnhandledGraphQLException(INTERNAL_SERVER_ERROR, msg)
        }

    abstract class IrrecoverableGraphQLException(status : HttpStatus, msg : String) : IrrecoverableIntegrationException("$msg (${status.value()})",
        null, null) {

        class UnexpectedResponseGraphQLException(status : HttpStatus, msg : String) : IrrecoverableGraphQLException(status, msg)

        class NotFoundGraphQLException(status : HttpStatus, msg : String) : IrrecoverableGraphQLException(status, msg)
        class BadGraphQLException(status : HttpStatus, msg : String) : IrrecoverableGraphQLException(status, msg)
        class UnauthenticatedGraphQLException(status : HttpStatus, msg : String) : IrrecoverableGraphQLException(status, msg)
        class UnauthorizedGraphQLException(status : HttpStatus, msg : String) : IrrecoverableGraphQLException(status, msg)
    }

    abstract class RecoverableGraphQLException(status : HttpStatus, msg : String, cause : Throwable?)
        : RecoverableIntegrationException("${status.value()}-$msg", cause = cause) {

        class UnhandledGraphQLException(status : HttpStatus, msg : String, cause : Throwable? = null) : RecoverableGraphQLException(status, msg, cause)
    }
}