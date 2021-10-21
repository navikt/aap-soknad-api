package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException

interface PDLErrorHandler {
    fun <T> handleError(e: GraphQLErrorsException): T
}