package no.nav.aap.api.søknad.joark

import no.nav.aap.api.søknad.joark.pdf.PDFClient
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.stereotype.Service

@Service
class JoarkLeverandør(private val joark: JoarkClient,
                      private val pdf: PDFClient,
                      private val converter: JoarkConverter) {

    fun leverSøknad(søknad: StandardSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            LeveranseResultat(this, joark.journalfør(converter.convert(søknad, søker, this)))
        }

    fun leverSøknad(søknad: UtlandSøknad, søker: Søker) =
        with(pdf.generate(søker, søknad)) {
            LeveranseResultat(this, joark.journalfør(converter.convert(søknad, søker, this)))
        }
}

data class LeveranseResultat(val pdf: ByteArray, val journalpostId: String)