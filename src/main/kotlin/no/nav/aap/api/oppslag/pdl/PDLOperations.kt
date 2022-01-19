package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.api.søknad.model.Person

interface PDLOperations {
    fun person(): Person?
}

interface PDLErrorHandler {
    fun <T> handleError(e: GraphQLErrorsException): T
}