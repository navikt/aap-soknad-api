package no.nav.aap.api.mellomlagring

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.SkjemaType

interface Mellomlagring {
    fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String)
    fun les(fnr: Fødselsnummer, type: SkjemaType): String?
    fun slett(fnr: Fødselsnummer, type: SkjemaType): Boolean
}