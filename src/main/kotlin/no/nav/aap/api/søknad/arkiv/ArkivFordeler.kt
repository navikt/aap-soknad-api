package no.nav.aap.api.søknad.arkiv

import no.nav.aap.api.søknad.arkiv.pdf.PDFClient
import no.nav.aap.api.søknad.ettersendelse.Ettersending
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Service

@Service
class ArkivFordeler(private val arkiv: ArkivClient,
                    private val pdf: PDFClient,
                    private val generator: ArkivJournalpostGenerator) {
    private val log = getLogger(javaClass)

    fun fordel(søknad: StandardSøknad, søker: Søker) =
        with(pdf.tilPdf(søker, søknad)) {
            ArkivSøknadResultat(this, arkiv.journalfør(generator.journalpostFra(søknad, søker, this))).also {
                log.trace("Fordeling til arkiv OK med journalpost ${it.journalpostId}")
            }
        }

    fun fordel(søknad: UtlandSøknad, søker: Søker) =
        with(pdf.tilPdf(søker, søknad)) {
            ArkivSøknadResultat(this, arkiv.journalfør(generator.journalpostFra(søknad, søker, this)))
        }

    fun fordel(ettersending: Ettersending, søker: Søker) =
        ArkivEttersendingResultat(arkiv.journalfør(generator.journalpostFra(ettersending, søker))).also {
            log.trace("Fordeling av ettersending til arkiv OK med journalpost ${it.journalpostId}")
        }

    data class ArkivSøknadResultat(val pdf: ByteArray, val journalpostId: String)
    data class ArkivEttersendingResultat(val journalpostId: String)

}