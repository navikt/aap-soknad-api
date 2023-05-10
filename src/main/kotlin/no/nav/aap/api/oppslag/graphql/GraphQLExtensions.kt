package no.nav.aap.api.oppslag.graphql

import org.springframework.graphql.client.FieldAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.BadRequest
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.NotFound
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.Unauthenticated
import no.nav.aap.api.oppslag.graphql.GraphQLErrorHandler.Companion.Unauthorized
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.RecoverableGraphQL.UnhandledGraphQL
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.UnrecoverableGraphQL.BadGraphQL
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.UnrecoverableGraphQL.NotFoundGraphQL
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.UnrecoverableGraphQL.UnauthenticatedGraphQL
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.UnrecoverableGraphQL.UnauthorizedGraphQL
import no.nav.aap.util.LoggerUtil

object GraphQLExtensions {

    private val log = LoggerUtil.getLogger(javaClass)

    const val IDENT = "ident"
    const val IDENTER = "identer"

    fun FieldAccessException.oversett() = oversett(response.errors.firstOrNull()?.extensions?.get("code")?.toString(), message ?: "Ukjent feil").also { e ->
        log.warn("GraphQL oppslag returnerte ${response.errors.size} feil. ${response.errors}, oversatte feilkode til ${e.javaClass.simpleName}",
            this)
    }

    private fun oversett(kode: String?, msg: String) =
        when (kode) {
            Unauthorized -> UnauthorizedGraphQL(UNAUTHORIZED,msg)
            Unauthenticated -> UnauthenticatedGraphQL(FORBIDDEN,msg)
            BadRequest -> BadGraphQL(BAD_REQUEST, msg)
            NotFound -> NotFoundGraphQL(NOT_FOUND, msg)
            else -> UnhandledGraphQL(INTERNAL_SERVER_ERROR,msg)
        }
    abstract class UnrecoverableGraphQL(status: HttpStatus, msg: String) : Throwable("${status.value()}-$msg", null) {
        class NotFoundGraphQL(status: HttpStatus, msg: String) : UnrecoverableGraphQL(status,msg)
        class BadGraphQL(status: HttpStatus, msg: String) : UnrecoverableGraphQL(status,msg)
        class UnauthenticatedGraphQL(status: HttpStatus, msg: String) : UnrecoverableGraphQL(status,msg)
        class UnauthorizedGraphQL(status: HttpStatus, msg: String) : UnrecoverableGraphQL(status,msg)

    }
    abstract class RecoverableGraphQL(status: HttpStatus, msg: String) : Throwable("${status.value()}-$msg", null) {
        class UnhandledGraphQL(status: HttpStatus, msg: String) : RecoverableGraphQL(status,msg)
    }
}