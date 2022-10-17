package no.nav.aap.api.søknad.mellomlagring

import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean

@ConditionalOnMissingBean(GCPKryptertMellomlager::class)
class InMemoryMellomlager : Mellomlager {
    private val store = mutableMapOf<String, String>()
    override fun lagre(value: String, type: SkjemaType) = navn((Fødselsnummer("08089403198")), type).also { store[it] = value }

    override fun les(type: SkjemaType) = store[navn(Fødselsnummer("08089403198"), type)]

    override fun slett(type: SkjemaType) = store.remove(navn((Fødselsnummer("08089403198")), type)) != null
    override fun ikkeOppdatertSiden(duration: Duration): List<Triple<Fødselsnummer, LocalDateTime, UUID>> {
        TODO("Not yet implemented")
    }
}