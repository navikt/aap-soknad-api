package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.felles.UtenlandsSøknadKafka
import org.springframework.stereotype.Component
import java.util.Base64.getEncoder

@Component
class PDFGenerator(val adapter: PDFGeneratorAdapter) {
    fun generate(søknad: UtenlandsSøknadKafka) = getEncoder().encodeToString(adapter.generate(søknad))
}