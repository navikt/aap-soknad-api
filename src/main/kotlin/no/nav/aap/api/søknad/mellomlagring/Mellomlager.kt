package no.nav.aap.api.søknad.mellomlagring

import java.time.LocalDateTime
import java.util.Objects.hash
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD

interface Mellomlager {
    fun lagre(value: String, type: SkjemaType): String
    fun les(type: SkjemaType): String?
    fun slett(type: SkjemaType = STANDARD): Boolean
    fun navn(fnr: Fødselsnummer, type: SkjemaType) = "${fnr.fnr}/${hash(type.name, fnr)}"

    fun alleBrukere() : List<Pair<String, LocalDateTime>>
}