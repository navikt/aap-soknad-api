package no.nav.aap.api.søknad.routing.standard

import no.nav.aap.api.oppslag.pdl.PDLClient
import no.nav.aap.api.søknad.brukernotifikasjoner.DittNavClient
import no.nav.aap.api.søknad.joark.JoarkRouter
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.stereotype.Component

@Component
class StandardSøknadRouter(private val joarkRouter: JoarkRouter,
                           private val pdl: PDLClient,
                           private val finalizer: StandardSøknadFinalizer,
                           private val vlRouter: StandardSøknadVLRouter) {

    fun route(søknad: StandardSøknad) =
        with(pdl.søkerMedBarn()) outer@{
            with(joarkRouter.route(søknad, this)) {
                vlRouter.route(søknad, this@outer, journalpostId)
                finalizer.finalize(søknad, pdf)
            }
        }
}

@Component
class StandardSøknadFinalizer(private val dittnav: DittNavClient,
                              private val dokumentLager: Dokumentlager) {
    fun finalize(søknad: StandardSøknad, pdf: ByteArray) =
        dokumentLager.finalize(søknad).run {
            dittnav.finalize()
            Kvittering(dokumentLager.lagreDokument(DokumentInfo(pdf, APPLICATION_PDF_VALUE, "kvittering.pdf")))
        }
}