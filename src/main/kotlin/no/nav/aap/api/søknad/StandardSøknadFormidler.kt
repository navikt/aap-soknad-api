package no.nav.aap.api.søknad

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.UtenlandsSøknadKafka
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.api.søknad.joark.JoarkClient
import no.nav.aap.api.søknad.joark.pdf.PDFGenerator
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.joark.AvsenderMottaker
import no.nav.aap.joark.Bruker
import no.nav.aap.joark.Dokument
import no.nav.aap.joark.DokumentVariant
import no.nav.aap.joark.Journalpost
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component

@Component
class StandardSøknadFormidler(private val joark: JoarkClient, private val pdfGen: PDFGenerator,private val pdl: PDLClient, private val kafka: StandardSøknadKafkaFormidler) {

    private val log = LoggerUtil.getLogger(javaClass)

    fun formidle(søknad: StandardSøknad) {
        var beriketSøknad = søknad.berik(pdl)
        joark.opprettJournalpost(
                Journalpost(
                dokumenter = docs(beriketSøknad),
                tittel = "Søknad om å beholde AAP ved opphold i utlandet",
                avsenderMottaker = AvsenderMottaker(beriketSøknad.søker.fødselsnummer, navn=beriketSøknad.fulltNavn),
                bruker = Bruker(beriketSøknad.søker!!.fødselsnummer)))
            .also {  log.info("Journalført $it OK") }
        kafka.formidle(beriketSøknad)
    }

    private fun docs(beriketSøknad: StandardSøknadBeriket)  =
        listOf(
                Dokument(
                "Søknad om å beholde AAP ved opphold i utlandet",
                "NAV 11-03.07",
                listOf(DokumentVariant(fysiskDokument = pdfGen.generate(beriketSøknad)))))
}

data class StandardSøknadBeriket(val søknad: StandardSøknad, val søker: Søker) {
    @JsonIgnore
    val fulltNavn = søker.navn.navn
}

fun StandardSøknad.berik(pdl: PDLClient): StandardSøknadBeriket {
    val søker =  pdl.søkerMedBarn()!!
    return StandardSøknadBeriket(this,søker)
}