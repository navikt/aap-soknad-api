package no.nav.aap.api.søknad.joark

import no.nav.aap.api.søknad.joark.pdf.PDFClient
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.stereotype.Service

@Service
class JoarkFordeler(private val joark: JoarkClient,
                    private val pdf: PDFClient,
                    private val converter: JoarkConverter) {

    fun fordel(søknad: StandardSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            FordelingResultat(this, joark.journalfør(converter.convert(søknad, søker, this)))
        }

    fun fordel(søknad: UtlandSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            FordelingResultat(this, joark.journalfør(converter.convert(søknad, søker, this)))
        }
}

data class FordelingResultat(val pdf: ByteArray, val journalpostId: String)