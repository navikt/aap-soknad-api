package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.Innsending
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Service

@Service
class ArkivFordeler(private val arkiv: ArkivClient, private val generator: ArkivJournalpostGenerator) {

    fun fordel(innsending: Innsending, søker: Søker) = arkiv.journalfør(generator.journalpostFra(innsending, søker))

    fun fordel(søknad: UtlandSøknad, søker: Søker) = arkiv.journalfør(generator.journalpostFra(søknad, søker))

    fun fordel(ettersending: StandardEttersending, søker: Søker) = arkiv.journalfør(generator.journalpostFra(ettersending, søker))
}