package no.nav.aap.api.mellomlagring.virus

import org.springframework.stereotype.Component

@Component
class ClamAvVirusScanner(private val a: VirusScanWebClientAdapter) : VirusScanner {
    override fun scan(bytes: ByteArray, name: String?) =
        if (a.harVirus(bytes, name)) {
            throw AttachmentException("Virus ble funnet")
        }
        else Unit
}

interface VirusScanner {
    fun scan(bytes: ByteArray, name: String?)
}