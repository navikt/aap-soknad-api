package no.nav.aap.api.søknad.fordeling

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

@Component
class StandardSøknadFordeler(private val joark: JoarkFordeler,
                             private val pdl: PDLClient,
                             private val dittnav: DittNavClient,
                             private val avslutter: StandardSøknadAvslutter,
                             private val cfg: VLFordelingConfig,
                             private val vl: SøknadVLFordeler) {

    fun fordel(søknad: StandardSøknad) =
        pdl.søkerMedBarn().run {
            with(joark.fordel(søknad, this)) {
                vl.fordel(søknad, fnr, journalpostId, cfg.standard)
                dittnav.opprettBeskjed(fnr = fnr, tekst = "Vi har mottatt ${STANDARD.tittel}")
                avslutter.avsluttSøknad(søknad, pdf)
            }
        }
}

@Component
class StandardSøknadAvslutter(private val dokumentLager: Dokumentlager,
                              private val mellomlager: Mellomlager) {
    fun avsluttSøknad(søknad: StandardSøknad, pdf: ByteArray) =
        dokumentLager.slettDokumenter(søknad).run {
            mellomlager.slett(STANDARD)
            Kvittering(dokumentLager.lagreDokument(DokumentInfo(pdf, APPLICATION_PDF_VALUE, "kvittering.pdf")))
        }
}