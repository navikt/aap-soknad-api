package no.nav.aap.api.søknad.minside

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.aap.api.felles.Fødselsnummer

data class MinSideForside(@JsonProperty("@event_name")  val eventName: EventName, val ident: Fødselsnummer, val microfrontend_id: String = MICROFRONTEND_ID) {

    enum class EventName {
        enable,disable
    }
    companion object {
        private const val MICROFRONTEND_ID = "aap"
    }
}
