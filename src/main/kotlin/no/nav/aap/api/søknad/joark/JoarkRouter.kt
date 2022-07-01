package no.nav.aap.api.søknad.joark

import no.nav.aap.api.søknad.joark.pdf.PDFClient
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.stereotype.Service

@Service
class JoarkRouter(private val joark: JoarkClient,
                  private val pdf: PDFClient,
                  private val converter: JoarkConverter) {

    private val log = getLogger(javaClass)
    fun route(søknad: StandardSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            Pair(this, joark.journalfør(converter.convert(søknad, søker, this)))
        }

    fun route(søknad: UtlandSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            Pair(this, joark.journalfør(converter.convert(søknad, søker, this)))
        }
}