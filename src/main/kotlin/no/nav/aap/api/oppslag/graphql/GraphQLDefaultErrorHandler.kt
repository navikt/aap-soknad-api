package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.graphql.GraphQLExtensions.oversett

@Component
class GraphQLDefaultErrorHandler : GraphQLErrorHandler {

     override fun handle(e: Throwable): Nothing {
         when (e) {
             is GraphQLErrorsException -> { throw e.oversett() }
             else -> throw e
         }
    }
}