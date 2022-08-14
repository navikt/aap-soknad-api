package no.nav.aap.api.søknad.joark

import no.nav.aap.api.søknad.joark.pdf.PDFClient
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Service

@Service
class JoarkFordeler(private val joark: JoarkClient,
                    private val pdf: PDFClient,
                    private val generator: JoarkJournalpostGenerator) {
    private val log = LoggerUtil.getLogger(javaClass)

    fun fordel(søknad: StandardSøknad, søker: Søker) =
        with(pdf.tilPdf(søker, søknad)) {
            log.trace("Fordeler til JOARK")
            FordelingResultat(this, joark.journalfør(generator.journalpostFra(søknad, søker, this))).also {
                log.trace("Fordeling til JOARK OK $it")
            }
        }

    fun fordel(søknad: UtlandSøknad, søker: Søker) =
        with(pdf.tilPdf(søker, søknad)) {
            FordelingResultat(this, joark.journalfør(generator.journalpostFra(søknad, søker, this)))
        }

    data class FordelingResultat(val pdf: ByteArray, val journalpostId: String)
}