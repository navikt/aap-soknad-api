package no.nav.aap.api.søknad.arkiv

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.util.LoggerUtil.getLogger

@Component
@Observed
class ArkivClient(private val adapter: ArkivWebClientAdapter) {
    private val log = getLogger(javaClass)

    fun arkiver(jp: Journalpost) =
        with(adapter.opprettJournalpost(jp)) {
            ArkivResultat(journalpostId, dokIder,jp.tilVikafossen)
        }.also {
            log.info("Journalpost ${jp.dokumenter} med tittel ${jp.tittel} og tilleggsopplysninger ${jp.tilleggsopplysninger} fordelt til arkiv OK med resultat $it")
        }
    data class ArkivResultat(val journalpostId: String, val dokumentIds: List<String>, val vikafossen: Boolean)

}