package no.nav.aap.api.søknad.mellomlagring

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean

@ConditionalOnMissingBean(GCPKryptertMellomlager::class)
class InMemoryMellomlager : Mellomlager {
    private val store = mutableMapOf<String, String>()
    override fun lagre(fnr: Fødselsnummer, type: SkjemaType, value: String) =
        key(fnr, type).also { store[it] = value }

    override fun les(fnr: Fødselsnummer, type: SkjemaType) =
        store[key(fnr, type)]

    override fun slett(fnr: Fødselsnummer, type: SkjemaType) =
        store.remove(key(fnr, type)) != null
}