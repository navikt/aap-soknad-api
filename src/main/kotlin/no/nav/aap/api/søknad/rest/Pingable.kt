package no.nav.aap.api.søknad.rest

import java.net.URI

interface Pingable {
    fun ping()
    fun pingEndpoint(): URI
    fun name(): String
}