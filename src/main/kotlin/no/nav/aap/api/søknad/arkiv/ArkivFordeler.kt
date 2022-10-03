package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.søknad.model.Innsending
import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.stereotype.Service

@Service
class ArkivFordeler(private val arkiv: ArkivClient, private val generator: ArkivJournalpostGenerator) {

    fun fordel(innsending: Innsending, søker: Søker) = arkiv.arkiver(generator.journalpostFra(innsending, søker))

    fun fordel(søknad: UtlandSøknad, søker: Søker) = arkiv.arkiver(generator.journalpostFra(søknad, søker))

    fun fordel(ettersending: StandardEttersending, søker: Søker) =
        arkiv.arkiver(generator.journalpostFra(ettersending, søker))
}