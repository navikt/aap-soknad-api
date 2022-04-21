package no.nav.aap.api.søknad.formidling.utland

import no.nav.aap.api.felles.Søker
import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.SkjemaType.UTLAND
import no.nav.aap.api.søknad.formidling.UtlandSøknadKafkaFormidler
import no.nav.aap.api.søknad.joark.JoarkClient
import no.nav.aap.api.søknad.joark.pdf.PDFGenerator
import no.nav.aap.api.søknad.model.UtenlandsSøknad
import no.nav.aap.joark.AvsenderMottaker
import no.nav.aap.joark.Bruker
import no.nav.aap.joark.Dokument
import no.nav.aap.joark.DokumentVariant
import no.nav.aap.joark.Journalpost
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class UtlandSøknadFormidler(private val joark: JoarkClient,
                            private val pdfGen: PDFGenerator,
                            private val pdl: PDLClient,
                            private val ctx: AuthContext,
                            private val kafka: UtlandSøknadKafkaFormidler) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun formidle(søknad: UtenlandsSøknad)   {
        val beriketSøknad = søknad.berikSøknad(Søker(ctx.getFnr(), pdl.søkerUtenBarn().navn))
        joark.opprettJournalpost(Journalpost(
                dokumenter = docs(beriketSøknad),
                tittel = UTLAND.tittel,
                avsenderMottaker = AvsenderMottaker(ctx.getFnr(), navn=beriketSøknad.fulltNavn),
                bruker = Bruker(ctx.getFnr())))
            .also {  log.info("Journalført $it OK") }
        kafka.formidle(beriketSøknad)
}

private fun docs(beriketSøknad: UtenlandsSøknadKafka)  =
    listOf(Dokument(UTLAND.tittel, UTLAND.kode, listOf(DokumentVariant(fysiskDokument = pdfGen.generate(beriketSøknad)))))
}