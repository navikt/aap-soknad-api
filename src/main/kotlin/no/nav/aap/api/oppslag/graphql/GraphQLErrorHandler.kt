package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException

interface GraphQLErrorHandler {
    fun  handle(e: GraphQLErrorsException): Nothing
}