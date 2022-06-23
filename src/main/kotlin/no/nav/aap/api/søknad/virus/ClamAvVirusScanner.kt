package no.nav.aap.api.søknad.virus

import no.nav.aap.api.søknad.mellomlagring.GCPBucketConfig.DokumentException
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.api.søknad.virus.ScanResult.Result.FOUND
import no.nav.aap.api.søknad.virus.ScanResult.Result.NONE
import no.nav.aap.api.søknad.virus.ScanResult.Result.OK
import org.springframework.stereotype.Component

@Component
class ClamAvVirusScanner(private val a: VirusScanWebClientAdapter) : DokumentSjekker {
    override fun sjekk(dokument: DokumentInfo) =
        when (a.harVirus(dokument.bytes).result) {
            FOUND -> throw DokumentException("Virus funnet i ${dokument.filnavn}")
            NONE, OK -> Unit
        }
}