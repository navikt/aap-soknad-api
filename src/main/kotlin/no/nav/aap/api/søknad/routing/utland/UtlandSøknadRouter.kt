package no.nav.aap.api.søknad.routing.utland

import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.SkjemaType.UTLAND
import no.nav.aap.api.søknad.joark.JoarkClient
import no.nav.aap.api.søknad.joark.pdf.PDFGenerator
import no.nav.aap.api.søknad.joark.pdf.PDFGeneratorWebClientAdapter.UtlandData
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.api.søknad.routing.UtlandSøknadVLRouter
import no.nav.aap.api.søknad.routing.VLRouter
import no.nav.aap.joark.AvsenderMottaker
import no.nav.aap.joark.Bruker
import no.nav.aap.joark.Dokument
import no.nav.aap.joark.Journalpost
import no.nav.aap.joark.asPDFVariant
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class UtlandSøknadRouter(private val joark: JoarkClient,
                         private val pdfGen: PDFGenerator,
                         private val pdl: PDLClient,
                         private val vlRouter: VLRouter,
                         private val router: UtlandSøknadVLRouter) {

    private val log = LoggerUtil.getLogger(javaClass)
    fun route(søknad: UtlandSøknad) =
        with(pdl.søkerUtenBarn()) {
            joark.journalfør(Journalpost(dokumenter = docs(this,søknad),
                    tittel = UTLAND.tittel,
                    avsenderMottaker = AvsenderMottaker(this.fødselsnummer,
                            navn = this.navn.navn),
                    bruker = Bruker(this.fødselsnummer)))
            if (vlRouter.skalTilVL(søknad)) {
                router.route(UtlandData(this,søknad))
            }
        }
    private fun docs(søker: Søker, søknad: UtlandSøknad) =
        listOf(Dokument(UTLAND.tittel, UTLAND.kode, listOf(pdfGen.generate(søker,søknad).asPDFVariant())))
}