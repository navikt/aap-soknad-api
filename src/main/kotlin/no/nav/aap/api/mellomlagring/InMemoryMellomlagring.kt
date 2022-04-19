package no.nav.aap.api.mellomlagring

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.formidling.SkjemaType
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean

@ConditionalOnMissingBean(GCPMellomlagring::class)
class InMemoryMellomlagring : Mellomlagring {
    private val store = mutableMapOf<String, String>()

    override fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String) = key(fnr, type).also { store[this.toString()] = value }

    override fun les(fnr: Fødselsnummer, type: SkjemaType) = store[key(fnr, type)]
    override fun slett(fnr: Fødselsnummer, type: SkjemaType) = store.remove(key(fnr, type)) != null
}