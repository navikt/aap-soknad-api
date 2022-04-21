package no.nav.aap.api.søknad.formidling

import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.dittnav.DittNavMeldingProdusent
import no.nav.aap.api.søknad.joark.JoarkFormidler
import no.nav.aap.api.søknad.model.StandardSøknad
import org.springframework.stereotype.Component

@Component
class StandardSøknadFormidler(private val joark: JoarkFormidler, private val pdl: PDLClient, private val dittnav: DittNavMeldingProdusent,private val kafka: StandardSøknadKafkaFormidler) {
    
    fun formidle(søknad: StandardSøknad) =
        with(pdl.søkerMedBarn()) {
            joark.formidle(søknad, this)
            kafka.formidle(søknad, this)
            dittnav.opprettBeskjed("AAP-søknad mottatt")
        }
}