package no.nav.aap.api.søknad.mellomlagring

import org.springframework.integration.annotation.ServiceActivator
import org.springframework.stereotype.Component
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.ENDELIG_SLETTET
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.OPPDATERT
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.OPPRETTET
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.MellomlagringsHendelse
import no.nav.aap.api.søknad.mellomlagring.MellomlagringBeanConfig.Companion.STORAGE_CHANNEL
import no.nav.aap.api.søknad.minside.MinSideClient

@Component
class MellomlagringEventHandler(private val minside: MinSideClient) {

    @ServiceActivator(inputChannel = STORAGE_CHANNEL)
    fun handleEvent(h: MellomlagringsHendelse) =
        h.metadata?.let {
            with(it) {
                when(h.type) {
                    OPPRETTET ->  minside.opprettUtkast(fnr, "Du har en påbegynt $tittel", type, eventId)
                    OPPDATERT -> minside.oppdaterUtkast(fnr, "Du har en påbegynt $tittel", type)
                    ENDELIG_SLETTET -> minside.avsluttUtkast(fnr, type)
                    else -> Unit
                }
            }
        } ?: throw IllegalStateException("Fant ikke forventede metadata i event")
}