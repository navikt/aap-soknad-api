package no.nav.aap.api.søknad.mellomlagring.dokument

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.model.StandardSøknad
import org.apache.tika.Tika
import java.util.*

interface Dokumentlager {
    fun lesDokument(uuid: UUID): DokumentInfo?
    fun slettDokumenter(vararg uuids: UUID): Unit?
    fun slettDokumenter(søknad: StandardSøknad)
    fun lagreDokument(dokument: DokumentInfo): UUID
    fun navn(fnr: Fødselsnummer, uuid: UUID) = "${fnr.fnr}/$uuid"

}

data class DokumentInfo(val bytes: ByteArray,
                        val contentType: String?,
                        val filnavn: String?,
                        val createTime: Long = 0,
                        val contentDisposition: String? = null) {

    override fun toString() =
        "${javaClass.simpleName} [filnavn=$filnavn,contentDisposition=$contentDisposition,contentType=$contentType,createTime=$createTime,størrelse=${bytes.size} bytes]"
}

interface DokumentSjekker {
    fun sjekk(dokument: DokumentInfo)

    companion object {
        val TIKA = Tika()
    }
}