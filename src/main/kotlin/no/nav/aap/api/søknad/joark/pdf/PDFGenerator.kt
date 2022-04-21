package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import org.springframework.stereotype.Component
import java.util.Base64.getEncoder

@Component
class PDFGenerator(val a: PDFGeneratorAdapter) {
    fun generateEncoded(søknad: UtenlandsSøknadKafka) = getEncoder().encodeToString(a.generate(søknad))
    fun generateEncoded(søker: Søker, søknad: StandardSøknad) = getEncoder().encodeToString(a.generate(søker, søknad))
    fun generate(søker: Søker, søknad: StandardSøknad) = a.generate(søker, søknad)
}