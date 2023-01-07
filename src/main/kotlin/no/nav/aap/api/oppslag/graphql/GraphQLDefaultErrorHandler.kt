package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.api.oppslag.graphql.GraphQLErrorExtensions.oversett
import org.springframework.stereotype.Component

@Component
class GraphQLDefaultErrorHandler : GraphQLErrorHandler {

     override fun handle(query: String,e: Throwable): Nothing {
         when (e) {
             is GraphQLErrorsException -> { throw e.oversett() }
             else -> throw e
         }
    }
}