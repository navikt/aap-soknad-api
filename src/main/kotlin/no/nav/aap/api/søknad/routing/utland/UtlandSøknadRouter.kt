package no.nav.aap.api.søknad.routing.utland

import no.nav.aap.api.felles.Søker
import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.SkjemaType.UTLAND
import no.nav.aap.api.søknad.routing.UtlandSøknadKafkaRouter
import no.nav.aap.api.søknad.joark.JoarkClient
import no.nav.aap.api.søknad.joark.pdf.PDFGenerator
import no.nav.aap.api.søknad.model.UtenlandsSøknad
import no.nav.aap.joark.AvsenderMottaker
import no.nav.aap.joark.Bruker
import no.nav.aap.joark.Dokument
import no.nav.aap.joark.Journalpost
import no.nav.aap.joark.asPDFVariant
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class UtlandSøknadRouter(private val joark: JoarkClient,
                         private val pdfGen: PDFGenerator,
                         private val pdl: PDLClient,
                         private val ctx: AuthContext,
                         private val kafka: UtlandSøknadKafkaRouter) {

    private val log = LoggerUtil.getLogger(javaClass)
    fun route(søknad: UtenlandsSøknad) =
        with(søknad.berikSøknad(Søker(ctx.getFnr(), pdl.søkerUtenBarn().navn))) {
            joark.journalfør(
                    Journalpost(dokumenter = docs(this),
                    tittel = UTLAND.tittel,
                    avsenderMottaker = AvsenderMottaker(ctx.getFnr(), navn = this.fulltNavn),
                    bruker = Bruker(ctx.getFnr())))
                .also { log.info("Journalført $it OK") }
            kafka.route(this)
        }
    private fun docs(søknad: UtenlandsSøknadKafka) = listOf(Dokument(UTLAND.tittel, UTLAND.kode, listOf(pdfGen.generate(søknad).asPDFVariant())))
}