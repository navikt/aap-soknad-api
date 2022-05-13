package no.nav.aap.api.mellomlagring.virus

import no.nav.aap.api.mellomlagring.virus.ScanResult.Result.FOUND
import no.nav.aap.api.mellomlagring.virus.ScanResult.Result.NONE
import no.nav.aap.api.mellomlagring.virus.ScanResult.Result.OK
import org.springframework.stereotype.Component

@Component
class ClamAvVirusScanner(private val a: VirusScanWebClientAdapter) : VirusScanner {
    override fun scan(bytes: ByteArray, name: String?) =
        when (a.harVirus(bytes).result) {
            FOUND -> throw AttachmentException("Virus funnet i $name")
            NONE, OK -> Unit
        }
}

interface VirusScanner {
    fun scan(bytes: ByteArray, name: String?)
}