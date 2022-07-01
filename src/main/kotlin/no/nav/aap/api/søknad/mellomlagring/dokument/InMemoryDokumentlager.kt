package no.nav.aap.api.søknad.mellomlagring.dokument

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.mellomlagring.GCPKMSKeyKryptertMellomlager
import no.nav.aap.api.søknad.model.StandardSøknad
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import java.util.*

@ConditionalOnMissingBean(GCPKMSKeyKryptertMellomlager::class)
class InMemoryDokumentlager : Dokumentlager {
    private val store = mutableMapOf<String, String>()

    override fun lesDokument(fnr: Fødselsnummer, uuid: UUID) = null

    override fun slettDokument(fnr: Fødselsnummer, uuid: UUID) = true
    override fun slettDokumenter(fnr: Fødselsnummer, søknad: StandardSøknad) {
        TODO("Not yet implemented")
    }

    override fun lagreDokument(fnr: Fødselsnummer,
                               dokument: DokumentInfo) = UUID.randomUUID()
}