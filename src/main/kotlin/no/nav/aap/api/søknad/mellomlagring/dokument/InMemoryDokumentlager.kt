package no.nav.aap.api.søknad.mellomlagring.dokument

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.mellomlagring.GCPKryptertMellomlager
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import java.util.*

@ConditionalOnMissingBean(GCPKryptertMellomlager::class)
class InMemoryDokumentlager : Dokumentlager {
    private val store = mutableMapOf<String, String>()

    override fun lesDokument(fnr: Fødselsnummer, uuid: UUID) = null

    override fun slettDokument(uuid: UUID, fnr: Fødselsnummer) = true

    override fun lagreDokument(fnr: Fødselsnummer,
                               dokument: DokumentInfo) = UUID.randomUUID()
}