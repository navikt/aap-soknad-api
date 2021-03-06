package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.UtlandSøknad
import org.springframework.stereotype.Component

@Component
class PDFGenerator(val a: PDFGeneratorWebClientAdapter) :PDFClient {
    override fun generate(søker: Søker,søknad: UtlandSøknad) = a.generate(søker,søknad)
    override fun generate(søker: Søker, søknad: StandardSøknad) = a.generate(søker, søknad)
}

interface PDFClient {
    fun generate(søker: Søker, søknad: StandardSøknad): ByteArray
    fun generate(søker: Søker, søknad: UtlandSøknad): ByteArray
}