package no.nav.aap.api.søknad.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException

interface PDLErrorHandler {
    fun <T> handleError(e: GraphQLErrorsException): T
}