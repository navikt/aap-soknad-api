package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException

interface GraphQLErrorHandler {
    fun handle(e: GraphQLErrorsException): Nothing
}

object GraphQLErrorCodes  {
    const val Unauthorized = "unauthorized"
    const val Unauthenticated = "unauthenticated"
    const val BadRequeest = "bad_request"
    const val NotFound = "not_found"
}