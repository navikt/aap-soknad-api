package no.nav.aap.api.søknad.arkiv

import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Component

@Component
class ArkivClient(private val adapter: ArkivWebClientAdapter) {
    private val log = getLogger(javaClass)

    fun journalfør(journalpost: Journalpost) =
        with(adapter.opprettJournalpost(journalpost))  {
            ArkivResultat(journalpostId,dokIder)
        }.also {
            log.trace("Fordeling av journalpost $journalpost til arkiv OK med id ${it.journalpostId}")
        }

    data class ArkivResultat(val journalpostId: String, val dokumentIds: List<String>)

}