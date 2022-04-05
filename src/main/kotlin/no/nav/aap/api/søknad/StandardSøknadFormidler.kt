package no.nav.aap.api.søknad

import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.joark.JoarkClient
import no.nav.aap.api.søknad.joark.pdf.PDFGenerator
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.util.AuthContext
import org.springframework.stereotype.Component

@Component
class StandardSøknadFormidler(private val joark: JoarkClient,
                              private val pdfGen: PDFGenerator,
                              private val pdl: PDLClient,
                              private val ctx: AuthContext,
                              private val kafka: StandardSøknadKafkaFormidler) {
    fun formidle(søknad: StandardSøknad) {
        // arkiver her
        var beriketSøknad = StandardSøknadKafka(ctx.getFnr(), pdl.søkerUtenBarn()?.fødseldato)
        kafka.formidle(beriketSøknad)
    }
}