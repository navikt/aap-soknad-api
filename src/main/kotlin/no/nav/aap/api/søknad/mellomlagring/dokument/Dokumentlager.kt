package no.nav.aap.api.søknad.mellomlagring.dokument

import no.nav.aap.api.felles.Fødselsnummer
import org.apache.tika.Tika
import java.util.*
import java.util.Objects.hash

interface Dokumentlager {
    fun lesDokument(fnr: Fødselsnummer, uuid: UUID): DokumentInfo?
    fun slettDokument(uuid: UUID, fnr: Fødselsnummer): Boolean
    fun lagreDokument(fnr: Fødselsnummer, dokument: DokumentInfo): UUID
    fun key(fnr: Fødselsnummer, uuid: UUID) = "${hash(fnr, uuid)}"

    companion object {
        const val FILNAVN = "filnavn"
        const val FNR = "fnr"
    }
}

interface DokumentSjekker {
    fun sjekk(dokument: DokumentInfo)
}

data class DokumentInfo(val bytes: ByteArray, val contentType: String?, val filnavn: String?) {
    constructor(bytes: ByteArray, filnavn: String?) : this(bytes, TIKA.detect(bytes), filnavn)

    init {
        require(TIKA.detect(bytes) == contentType) {
            "Foventet $contentType men fikk ${TIKA.detect(bytes)} for $filnavn"
        }
    }

    companion object {
        private val TIKA = Tika()
    }

    override fun toString() =
        "${javaClass.simpleName} [filnavn=$filnavn,contentType=$contentType,størrelse=${bytes.size} bytes]"
}