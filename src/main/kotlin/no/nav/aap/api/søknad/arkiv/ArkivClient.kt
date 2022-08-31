package no.nav.aap.api.søknad.arkiv

import no.nav.aap.joark.JoarkResponse
import no.nav.aap.joark.Journalpost
import org.springframework.stereotype.Component

@Component
class ArkivClient(private val adapter: ArkivWebClientAdapter) {
    fun journalfør(journalpost: Journalpost) =
        adapter.opprettJournalpost(journalpost).let(JoarkResponse::journalpostId)
}