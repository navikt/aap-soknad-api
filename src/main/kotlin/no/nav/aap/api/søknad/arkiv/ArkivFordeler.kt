package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.StandardSøknadMedKvittering
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Service

@Service
class ArkivFordeler(private val arkiv: ArkivClient, private val generator: ArkivJournalpostGenerator) {
    private val log = getLogger(javaClass)

    fun fordel(søknad: StandardSøknadMedKvittering, søker: Søker) =
        with(arkiv.journalfør(generator.journalpostFra(søknad, søker))) {
            ArkivResultat(journalpostId, dokumenter.map { it.dokumentInfoId }).also {
                log.trace("Fordeling av søknad til arkiv OK med journalpost ${it.journalpostId}")
            }
        }

    fun fordel(søknad: UtlandSøknad, søker: Søker) =
        with(arkiv.journalfør(generator.journalpostFra(søknad, søker))) {
            ArkivResultat(journalpostId, dokumenter.map { it.dokumentInfoId }).also {
                log.trace("Fordeling av utlandsøknad til arkiv OK med journalpost ${it.journalpostId}")
            }
        }

    fun fordel(ettersending: StandardEttersending, søker: Søker) =
        with(arkiv.journalfør(generator.journalpostFra(ettersending, søker))){
            ArkivResultat(journalpostId, dokumenter.map { it.dokumentInfoId }).also {
                log.trace("Fordeling av ettersending til arkiv OK med journalpost ${it.journalpostId}")
            }
        }
    data class ArkivResultat(val journalpostId: String, val dokumentIds: List<String>)
}