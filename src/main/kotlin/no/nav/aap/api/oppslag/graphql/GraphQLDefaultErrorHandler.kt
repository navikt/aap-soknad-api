package no.nav.aap.api.oppslag.graphql

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.springframework.graphql.client.FieldAccessException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.IrrecoverableGraphQLException.UnexpectedResponseGraphQLException
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.RecoverableGraphQLException.UnhandledGraphQLException
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.oversett

@Component
class GraphQLDefaultErrorHandler : GraphQLErrorHandler {

    override fun handle(e : Throwable) : Nothing {
        when (e) {
            is MismatchedInputException -> throw UnexpectedResponseGraphQLException(BAD_REQUEST, "Ikke-håndtert respons")
            is FieldAccessException -> throw e.oversett()
            else -> throw UnhandledGraphQLException(INTERNAL_SERVER_ERROR, "GraphQL oppslag  feilet", e)
        }
    }
}