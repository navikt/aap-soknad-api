package no.nav.aap.api.oppslag.saf

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException

interface SafErrorHandler {
    fun <T> handleError(e: GraphQLErrorsException): T
}