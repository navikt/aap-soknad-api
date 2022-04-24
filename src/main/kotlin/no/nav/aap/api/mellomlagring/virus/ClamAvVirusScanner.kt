package no.nav.aap.api.mellomlagring.virus

import org.springframework.stereotype.Component

@Component
class ClamAvVirusScanner(private val a: VirusScanWebClientAdapter) : VirusScanner {
    override fun scan(bytes: ByteArray, name: String?) = a.scan(bytes,name)
}

interface VirusScanner {
     fun scan(bytes: ByteArray, name: String?)
}