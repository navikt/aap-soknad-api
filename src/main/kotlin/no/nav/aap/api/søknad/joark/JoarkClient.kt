package no.nav.aap.api.søknad.joark

import no.nav.aap.joark.JoarkResponse
import no.nav.aap.joark.Journalpost
import org.springframework.stereotype.Component

@Component
class JoarkClient(private val adapter: JoarkWebClientAdapter) {
    fun journalfør(journalpost: Journalpost) =
        adapter.opprettJournalpost(journalpost).let(JoarkResponse::journalpostId)
}