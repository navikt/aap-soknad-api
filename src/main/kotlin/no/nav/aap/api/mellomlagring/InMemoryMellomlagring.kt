package no.nav.aap.api.mellomlagring

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.SkjemaType
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean

@ConditionalOnMissingBean(GCPMellomlagring::class)
class InMemoryMellomlagring : Mellomlagring {
    private val store = mutableMapOf<String, String>()

    override fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String) {
        store[key(fnr, type)] = value
    }

    override fun les(fnr: Fødselsnummer, type: SkjemaType) = store[key(fnr, type)]
    override fun slett(fnr: Fødselsnummer, type: SkjemaType) = store.remove(key(fnr, type)) != null
    private fun key(fnr: Fødselsnummer, type: SkjemaType) = fnr.fnr.plus("_").plus(type.name)
}