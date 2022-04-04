package no.nav.aap.api.søknad.joark

import no.nav.aap.joark.Journalpost
import org.springframework.stereotype.Component

@Component
class JoarkClient(private val adapter: JoarkClientAdapter) {
    fun opprettJournalpost(journalpost: Journalpost) = adapter.opprettJournalpost(journalpost)?.journalpostId
}