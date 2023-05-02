package no.nav.aap.api.søknad.arkiv

import org.springframework.stereotype.Service
import no.nav.aap.api.søknad.fordeling.Innsending
import no.nav.aap.api.søknad.fordeling.Ettersending
import no.nav.aap.api.oppslag.person.Søker

@Service
class ArkivFordeler(private val arkiv : ArkivClient, private val generator : ArkivJournalpostGenerator) {

    fun fordel(innsending : Innsending, søker : Søker) = arkiv.arkiver(generator.journalpostFra(innsending, søker))
    fun fordel(ettersending : Ettersending, søker : Søker, routing : Boolean) = arkiv.arkiver(generator.journalpostFra(ettersending, søker, routing))
}