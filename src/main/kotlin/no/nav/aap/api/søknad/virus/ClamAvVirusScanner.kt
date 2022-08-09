package no.nav.aap.api.søknad.virus

import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.DokumentException.Substatus.VIRUS
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.api.søknad.virus.ScanResult.Result.FOUND
import no.nav.aap.api.søknad.virus.ScanResult.Result.NONE
import no.nav.aap.api.søknad.virus.ScanResult.Result.OK
import org.springframework.stereotype.Component

@Component
class ClamAvVirusScanner(private val a: VirusScanWebClientAdapter) : DokumentSjekker {
    override fun sjekk(dokument: DokumentInfo) =
        with(dokument) {
            when (a.harVirus(bytes).result) {
                FOUND -> throw VirusException("Virus funnet i $filnavn")
                NONE, OK -> Unit
            }
        }
}

class VirusException(msg: String) : DokumentException(VIRUS, msg)