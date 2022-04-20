package no.nav.aap.api.søknad.formidling

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.api.mellomlagring.Vedlegg
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.joark.JoarkFormidler
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.util.LoggerUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StandardSøknadFormidler(private val joark: JoarkFormidler, private val pdl: PDLClient, private val kafka: StandardSøknadKafkaFormidler) {


    fun formidle(søknad: StandardSøknad) =
        with(pdl.søkerMedBarn()) {
            joark.formidle(this, søknad)
            kafka.formidle(this, søknad)
        }
}