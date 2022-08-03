package no.nav.aap.api.søknad.mellomlagring

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean

@ConditionalOnMissingBean(GCPKryptertMellomlager::class)
class InMemoryMellomlager : Mellomlager {
    private val store = mutableMapOf<String, String>()
    override fun lagre(type: SkjemaType, value: String) =
        navn((Fødselsnummer("08089403198")), type).also { store[it] = value }

    override fun les(type: SkjemaType) =
        store[navn(Fødselsnummer("08089403198"), type)]

    override fun slett(type: SkjemaType) =
        store.remove(navn((Fødselsnummer("08089403198")), type)) != null
}