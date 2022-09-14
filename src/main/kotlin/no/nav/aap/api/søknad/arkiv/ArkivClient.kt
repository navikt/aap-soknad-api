package no.nav.aap.api.søknad.arkiv

import no.nav.aap.arkiv.Journalpost
import org.springframework.stereotype.Component

@Component
class ArkivClient(private val adapter: ArkivWebClientAdapter) {
    fun journalfør(journalpost: ArkivJournalpost) =
        adapter.opprettJournalpost(journalpost).journalpostId
}