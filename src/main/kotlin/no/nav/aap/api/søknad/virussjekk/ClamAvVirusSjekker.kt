package no.nav.aap.api.søknad.virussjekk

import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import no.nav.aap.api.error.Substatus.VIRUS
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentSjekker
import no.nav.aap.api.søknad.virussjekk.ScanResult.Result.FOUND
import no.nav.aap.api.søknad.virussjekk.ScanResult.Result.NONE
import no.nav.aap.api.søknad.virussjekk.ScanResult.Result.OK

@Component
@Observed
class ClamAvVirusSjekker(private val adapter: VirusScanWebClientAdapter) : DokumentSjekker {
    override fun sjekk(dokument: DokumentInfo) =
        with(dokument) {
            when (adapter.harVirus(bytes).result) {
                FOUND -> throw VirusException("Virus funnet i $filnavn, kan ikke lastes opp")
                NONE, OK -> Unit
            }
        }
}

class VirusException(msg: String) : DokumentException(msg, null, VIRUS)