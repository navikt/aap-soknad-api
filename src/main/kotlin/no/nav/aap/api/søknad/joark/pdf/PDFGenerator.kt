package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import org.springframework.stereotype.Component
import java.util.Base64.getEncoder

@Component
class PDFGenerator(val a: PDFGeneratorAdapter) {
    fun generate(søknad: UtenlandsSøknadKafka) = getEncoder().encodeToString(a.generate(søknad))
    fun generate(søker: Søker, søknad: StandardSøknad) = getEncoder().encodeToString(a.generate(søker, søknad))
}