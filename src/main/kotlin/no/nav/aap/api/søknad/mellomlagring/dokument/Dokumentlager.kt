package no.nav.aap.api.søknad.mellomlagring.dokument

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.model.StandardSøknad
import java.util.*
import java.util.Objects.hash

interface Dokumentlager {
    fun lesDokument(uuid: UUID): DokumentInfo?
    fun slettDokument(uuid: UUID): Boolean
    fun slettDokumenter(søknad: StandardSøknad)
    fun lagreDokument(dokument: DokumentInfo): UUID
    fun mavn(fnr: Fødselsnummer, uuid: UUID) = "${hash(fnr, uuid)}"

    companion object {
        const val FILNAVN = "filnavn"
        const val FNR = "fnr"
    }

}

data class DokumentInfo(val bytes: ByteArray,
                        val contentType: String?,
                        val filnavn: String?,
                        val createTime: Long = 0) {

    override fun toString() =
        "${javaClass.simpleName} [filnavn=$filnavn,contentType=$contentType,createTime=$createTime,størrelse=${bytes.size} bytes]"
}

interface DokumentSjekker {
    fun sjekk(dokument: DokumentInfo)
}