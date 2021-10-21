package no.nav.aap.api.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException

interface PDLErrorHandler {
    fun <T> handleError(e: GraphQLErrorsException): T
}