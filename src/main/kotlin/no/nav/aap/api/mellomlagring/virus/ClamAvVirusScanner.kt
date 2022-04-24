package no.nav.aap.api.mellomlagring.virus

import no.nav.aap.api.mellomlagring.virus.ScanResult.Result.OK
import org.springframework.stereotype.Component

@Component
class ClamAvVirusScanner(private val a: VirusScanWebClientAdapter) : VirusScanner {
    override fun scan(bytes: ByteArray, name: String?) = if (OK != a.scan(bytes,name)) throw AttachmentVirusException(name) else Unit
}

interface VirusScanner {
     fun scan(bytes: ByteArray, name: String?)
}