package no.nav.aap.api.søknad.mellomlagring

import org.springframework.integration.annotation.ServiceActivator
import org.springframework.stereotype.Component
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.ENDELIG_SLETTET
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.OPPDATERT
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.GCPEventType.OPPRETTET
import no.nav.aap.api.søknad.mellomlagring.GCPBucketEventTransformer.MellomlagringsHendelse
import no.nav.aap.api.søknad.minside.MinSideClient

@Component
class MellomlagringHendelseHåndterer(private val minside: MinSideClient) {

    @ServiceActivator(inputChannel = MellomlagringBeanConfig.STORAGE_CHANNEL)
    fun håndter(h: MellomlagringsHendelse) =
        h.metadata?.let { md ->
            when(h.type) {
                OPPRETTET ->  minside.opprettUtkast(md.fnr, "Du har en påbegynt $md.", md.type, md.eventId)
                OPPDATERT -> minside.oppdaterUtkast(md.fnr, "Du har en påbegynt ${md.tittel}", md.type)
                ENDELIG_SLETTET -> minside.avsluttUtkast(md.fnr, md.type)
                else -> Unit
            }
        } ?: throw IllegalStateException("Fant ikke forventede metadata i event}")
}