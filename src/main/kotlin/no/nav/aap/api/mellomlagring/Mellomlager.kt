package no.nav.aap.api.mellomlagring

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.SkjemaType
import java.util.Objects.hash

interface Mellomlager {
    fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String): String
    fun les(fnr: Fødselsnummer, type: SkjemaType): String?
    fun slett(fnr: Fødselsnummer, type: SkjemaType): Boolean
    fun key(fnr: Fødselsnummer, type: SkjemaType) = "${hash(type.name, fnr)}"
}