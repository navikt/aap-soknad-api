package no.nav.aap.api.mellomlagring.virus

import org.springframework.stereotype.Component

@Component
class ClamAvVirusScanner(private val a: VirusScanWebClientAdapter) : VirusScanner {
    override fun harVirus(bytes: ByteArray, name: String?) =
        if (a.harVirus(bytes,name)) {
            throw AttachmentVirusException(name)
            } else Unit
}

interface VirusScanner {
     fun harVirus(bytes: ByteArray, name: String?)
}