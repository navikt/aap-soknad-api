package no.nav.aap.api.søknad.joark

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.joark.pdf.PDFClient
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Service

@Service
class JoarkRouter(private val joark: JoarkClient,
                  private val pdf: PDFClient,
                  private val lager: Dokumentlager,
                  private val joarkConverter: JoarkConverter) {

    private val log = getLogger(javaClass)
    fun route(søknad: StandardSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            Pair(lagreKvittering(this, søker.fnr),
                    joark.journalfør(joarkConverter.convert(søknad, søker, this))
                        ?: throw IntegrationException("Kunne ikke journalføre søknad"))
        }

    fun route(søknad: UtlandSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            Pair(lagreKvittering(this, søker.fnr),
                    joark.journalfør(joarkConverter.convert(søknad, søker, this))
                        ?: throw IntegrationException("Kunne ikke journalføre søknad"))
        }

    private fun lagreKvittering(bytes: ByteArray, fnr: Fødselsnummer) =
        lager.lagreDokument(fnr, DokumentInfo(bytes, APPLICATION_PDF_VALUE, "kvittering.pdf"))
}