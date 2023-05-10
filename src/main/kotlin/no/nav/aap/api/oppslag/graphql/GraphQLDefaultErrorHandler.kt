package no.nav.aap.api.oppslag.graphql

import org.springframework.graphql.client.FieldAccessException
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.oversett

@Component
class GraphQLDefaultErrorHandler : GraphQLErrorHandler {

     override fun handle(e: Throwable, query: String): Nothing {
         when (e) {
             is FieldAccessException -> throw e.oversett()
             else -> throw e
         }
    }
}