package no.nav.aap.api.oppslag.arkiv

import org.springframework.stereotype.Component

@Component
class ArkivOppslagClient(private val a: ArkivOppslagWebClientAdapter) {
    fun dokument(journalpostId: String, dokumentId: DokumentInfoId) =
        a.dokument(journalpostId, dokumentId.dokumentInfoId)

    fun saker() = a.saker()
}