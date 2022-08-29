package no.nav.aap.api.søknad.joark

import no.nav.aap.api.søknad.ettersendelse.Ettersending
import no.nav.aap.api.søknad.joark.pdf.PDFClient
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Service

@Service
class JoarkFordeler(private val joark: JoarkClient,
                    private val pdf: PDFClient,
                    private val generator: JoarkJournalpostGenerator) {
    private val log = getLogger(javaClass)

    fun fordel(søknad: StandardSøknad, søker: Søker) =
        with(pdf.tilPdf(søker, søknad)) {
            JoarkSøknadResultat(this, joark.journalfør(generator.journalpostFra(søknad, søker, this))).also {
                log.trace("Fordeling til JOARK OK med journalpost ${it.journalpostId}")
            }
        }

    fun fordel(søknad: UtlandSøknad, søker: Søker) =
        with(pdf.tilPdf(søker, søknad)) {
            JoarkSøknadResultat(this, joark.journalfør(generator.journalpostFra(søknad, søker, this)))
        }

    fun fordel(ettersending: Ettersending, søker: Søker) =
        JoarkEttersendingResultat(joark.journalfør(generator.journalpostFra(ettersending, søker))).also {
            log.trace("Fordeling av ettersending til JOARK OK med journalpost ${it.journalpostId}")
        }

    data class JoarkSøknadResultat(val pdf: ByteArray, val journalpostId: String)
    data class JoarkEttersendingResultat(val journalpostId: String)

}