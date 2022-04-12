package no.nav.aap.api.søknad

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.SkjemaType.HOVED
import no.nav.aap.api.søknad.joark.JoarkClient
import no.nav.aap.api.søknad.joark.pdf.PDFGenerator
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.joark.AvsenderMottaker
import no.nav.aap.joark.Bruker
import no.nav.aap.joark.Dokument
import no.nav.aap.joark.DokumentVariant
import no.nav.aap.joark.Filtype.JSON
import no.nav.aap.joark.Filtype.PDFA
import no.nav.aap.joark.Journalpost
import no.nav.aap.joark.VariantFormat.ARKIV
import no.nav.aap.joark.VariantFormat.ORIGINAL
import no.nav.aap.util.LoggerUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StandardSøknadFormidler(private val joark: JoarkClient, private val pdf: PDFGenerator, private val pdl: PDLClient, private val kafka: StandardSøknadKafkaFormidler) {

    @Autowired
    private lateinit var mapper: ObjectMapper
    private val log = LoggerUtil.getLogger(javaClass)

    fun formidle(søknad: StandardSøknad) =
        with(pdl.søkerUtenBarn()) {
            joark.opprettJournalpost(
                    Journalpost(
                            dokumenter = docs(this, søknad),
                            tittel = HOVED.tittel,
                            avsenderMottaker = AvsenderMottaker(fødselsnummer, navn = navn.navn),
                            bruker = Bruker(fødselsnummer)))
                .also { log.info("Journalført søknad $it OK") }
            kafka.formidle(søknad, this)
                .also { log.info("Formidlet søknad til Kakfa OK") }
        }

    private fun docs(søker: Søker, søknad: StandardSøknad)  =
        listOf(Dokument(HOVED.tittel, HOVED.kode, listOf(
                DokumentVariant(JSON, søknad.toEncodedJson(mapper), ORIGINAL),
                DokumentVariant(PDFA, pdf.generate(søker, søknad), ARKIV))))
}