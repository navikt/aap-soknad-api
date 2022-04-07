package no.nav.aap.api.søknad

import no.nav.aap.api.felles.Søker
import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
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
class UtenlandSøknadFormidler(private val joark: JoarkClient,
                              private val pdfGen: PDFGenerator,
                              private val pdl: PDLClient,
                              private val ctx: AuthContext,
                              private val kafka: UtenlandsSøknadKafkaFormidler) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun formidle(søknad: UtenlandsSøknad)   {
        val beriketSøknad = søknad.berikSøknad(Søker(ctx.getFnr(), pdl.søkerUtenBarn()?.navn))
        joark.opprettJournalpost(Journalpost(
                dokumenter = docs(beriketSøknad),
                tittel = "Søknad om å beholde AAP ved opphold i utlandet",
                avsenderMottaker = AvsenderMottaker(ctx.getFnr(), navn=beriketSøknad.fulltNavn),
                bruker = Bruker(ctx.getFnr())))
            .also {  log.info("Journalført $it OK") }
        kafka.formidle(beriketSøknad)
}

private fun docs(beriketSøknad: UtenlandsSøknadKafka)  =
    listOf(Dokument(
            "Søknad om å beholde AAP ved opphold i utlandet",
            "NAV 11-03.07",
            listOf(DokumentVariant(fysiskDokument = pdfGen.generate(beriketSøknad)))))
}