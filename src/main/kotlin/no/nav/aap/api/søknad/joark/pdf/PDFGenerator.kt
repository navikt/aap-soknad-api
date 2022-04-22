package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import org.springframework.stereotype.Component

@Component
class PDFGenerator(val a: PDFGeneratorWebClientAdapter) {
    fun generate(søknad: UtenlandsSøknadKafka) = a.generate(søknad)
    fun generate(søker: Søker, søknad: StandardSøknad) = a.generate(søker, søknad)
}