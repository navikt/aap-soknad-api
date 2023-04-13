package no.nav.aap.api.søknad.arkiv

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.util.LoggerUtil.getLogger

@Component
@Observed
class ArkivClient(private val adapter: ArkivWebClientAdapter) {
    private val log = getLogger(javaClass)

    fun arkiver(journalpost: Journalpost) =
        with(adapter.opprettJournalpost(journalpost)) {
            ArkivResultat(journalpostId, dokIder)
        }.also {
            log.info("Journalpost ${journalpost.dokumenter} med tittel ${journalpost.tittel} og tilleggsopplysninger ${journalpost.tilleggsopplysninger} fordelt til arkiv OK med resultat $it")
        }
    data class ArkivResultat(val journalpostId: String, val dokumentIds: List<String>)

}