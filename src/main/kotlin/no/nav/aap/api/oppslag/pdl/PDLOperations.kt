package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.søknad.model.Søker

interface PDLOperations {
    fun søker(medBarn: Boolean = false): Søker?
}

interface PDLErrorHandler {
    fun <T> handleError(e: GraphQLErrorsException): T
}