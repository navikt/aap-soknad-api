package no.nav.aap.api.søknad.domain

import com.fasterxml.jackson.annotation.JsonValue

data class Fødselsnummer(@JsonValue val fnr: String)