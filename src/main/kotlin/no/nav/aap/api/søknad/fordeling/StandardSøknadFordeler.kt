package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.joark.JoarkFordeler
import no.nav.aap.api.søknad.mellomlagring.Mellomlager
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Component
import java.util.*

@Component
class StandardSøknadFordeler(private val joark: JoarkFordeler,
                             private val pdl: PDLClient,
                             private val avslutter: StandardSøknadAvslutter,
                             private val cfg: VLFordelingConfig,
                             private val vl: SøknadVLFordeler) {

    fun fordel(søknad: StandardSøknad) =
        with(pdl.søkerMedBarn()) outer@{
            with(joark.fordel(søknad, this)) {
                vl.fordel(søknad, this@outer.fnr, journalpostId, cfg.standard)
                avslutter.avsluttSøknad(søknad, this@outer.fnr, pdf)
            }
        }
}

@Component
class StandardSøknadAvslutter(private val dittnav: DittNavClient,
                              private val dokumentLager: Dokumentlager,
                              private val mellomlager: Mellomlager) {
    fun avsluttSøknad(søknad: StandardSøknad, fnr: Fødselsnummer, pdf: ByteArray) =
        dokumentLager.slettDokumenter(søknad).run {
            mellomlager.slett(STANDARD)
            dittnav.opprettBeskjed(STANDARD, UUID.randomUUID(), fnr, "Vi har mottatt ${STANDARD.tittel}")
            Kvittering(dokumentLager.lagreDokument(DokumentInfo(pdf, APPLICATION_PDF_VALUE, "kvittering.pdf")))
        }
}