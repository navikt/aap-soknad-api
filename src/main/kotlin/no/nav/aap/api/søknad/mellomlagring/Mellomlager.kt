package no.nav.aap.api.søknad.mellomlagring

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import java.util.Objects.hash

interface Mellomlager {
    fun lagre(type: SkjemaType, value: String): String
    fun les(type: SkjemaType): String?
    fun slett(type: SkjemaType): Boolean
    fun key(fnr: Fødselsnummer, type: SkjemaType) = "${hash(type.name, fnr)}"
}