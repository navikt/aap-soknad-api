package no.nav.aap.api.søknad

import no.nav.aap.api.felles.Søker
import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.joark.JoarkClient
import no.nav.aap.api.søknad.joark.pdf.PDFGenerator
import no.nav.aap.api.søknad.model.UtenlandsSøknadView
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
                              private val authContext: AuthContext,
                              private val kafka: KafkaUtenlandsSøknadFormidler) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun formidle(søknad: UtenlandsSøknadView)   {
        val kafkaSøknad = søknad.toKafkaObject(Søker(authContext.getFnr(), pdl.søkerUtenBarn()?.navn))
        joark.opprettJournalpost(Journalpost(
                dokumenter = docs(kafkaSøknad),
                tittel = "Søknad om å beholde AAP ved opphold i utlandet",
                avsenderMottaker = AvsenderMottaker(authContext.getFnr(), navn=kafkaSøknad.fulltNavn),
                bruker = Bruker(authContext.getFnr())))
            .also {  log.info("Journalført $it OK") }
        kafka.formidle(kafkaSøknad)
}

private fun docs(kafkaSøknad: UtenlandsSøknadKafka): List<Dokument> =
    listOf(Dokument(
            "Søknad om å beholde AAP ved opphold i utlandet",
            "NAV 11-03.07",
            listOf(DokumentVariant(fysiskDokument = pdfGen.generate(kafkaSøknad)))))
}