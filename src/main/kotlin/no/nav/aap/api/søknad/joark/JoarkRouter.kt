package no.nav.aap.api.søknad.joark

import no.nav.aap.api.søknad.joark.pdf.PDFClient
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.stereotype.Service

@Service
class JoarkRouter(private val joark: JoarkClient,
                  private val pdf: PDFClient,
                  private val converter: JoarkConverter) {

    fun route(søknad: StandardSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            RoutingResult(this, joark.journalfør(converter.convert(søknad, søker, this)))
        }

    fun route(søknad: UtlandSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            RoutingResult(this, joark.journalfør(converter.convert(søknad, søker, this)))
        }
}

data class RoutingResult(val pdf: ByteArray, val journalpostId: String)