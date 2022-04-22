package no.nav.aap.api.søknad.joark

import no.nav.aap.joark.Journalpost
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class JoarkClient(private val adapter: JoarkWebClientAdapter) {

    private val log = LoggerUtil.getLogger(javaClass)
    fun journalfør(journalpost: Journalpost) = adapter.opprettJournalpost(journalpost)
        .also { log.info("Journalført søknad $it OK") }
}