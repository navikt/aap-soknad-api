package no.nav.aap.api.felles

import com.fasterxml.jackson.annotation.JsonValue
import no.nav.aap.api.util.StringUtil.partialMask

data class Fødselsnummer(@JsonValue val fnr: String) {
    override fun toString() = partialMask(fnr)
}