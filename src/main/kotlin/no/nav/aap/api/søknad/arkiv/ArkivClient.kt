package no.nav.aap.api.søknad.arkiv

import org.springframework.stereotype.Component

@Component
class ArkivClient(private val adapter: ArkivWebClientAdapter) {
    fun journalfør(journalpost: Journalpost) =
        adapter.opprettJournalpost(journalpost).journalpostId
}