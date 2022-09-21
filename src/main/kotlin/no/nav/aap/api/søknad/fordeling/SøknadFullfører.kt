package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.STANDARD_ETTERSENDING
import no.nav.aap.api.felles.SkjemaType.UTLAND
import no.nav.aap.api.søknad.arkiv.ArkivFordeler.ArkivResultat
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Companion.SISTE_SØKNAD
import no.nav.aap.api.søknad.fordeling.SøknadRepository.Søknad
import no.nav.aap.api.søknad.mellomlagring.Mellomlager
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPSTD
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.Companion.MINAAPUTLAND
import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.StandardEttersending.EttersendtVedlegg
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil
import no.nav.aap.util.MDCUtil.callIdAsUUID
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.Locale.getDefault

@Component
class SøknadFullfører(private val dokumentLager: Dokumentlager,
                      private val minside: MinSideClient,
                      private val søknader: SøknadRepository,
                      private val mellomlager: Mellomlager) {

    private val log = getLogger(javaClass)

    fun fullfør(søknad: UtlandSøknad, fnr: Fødselsnummer, res: ArkivResultat) =
        minside.opprettBeskjed(type = MINAAPUTLAND, fnr = fnr, tekst = "Vi har mottatt ${UTLAND.tittel}", eksternNotifikasjon = true).run {
             Kvittering(res.journalpostId)
        }

    @Transactional
    fun fullfør(søknad: StandardSøknad, fnr: Fødselsnummer, res: ArkivResultat) =
        dokumentLager.slettDokumenter(søknad).run {
            mellomlager.slett()
            with(søknad.vedlegg()) {
                with(søknader.save(Søknad(fnr.fnr, res.journalpostId, MDCUtil.callIdAsUUID()))) {
                    registrerSomManglende(manglende)
                    registrerSomVedlagte(vedlagte)
                    oppdaterMinSide(manglende.isEmpty(), fnr)
                }
            }
            Kvittering(res.journalpostId)
        }

    @Transactional
    fun fullfør(e: StandardEttersending, fnr: Fødselsnummer, res: ArkivResultat) =
        dokumentLager.slettDokumenter(e).run {
            e.søknadId?.let {
                fullførEttersending(it, fnr, res, e.ettersendteVedlegg)
            } ?: fullførEttersendingUtenSøknad(fnr, res, e.ettersendteVedlegg)
            Kvittering(res.journalpostId)
        }

    private fun fullførEttersending(søknadId: UUID, fnr: Fødselsnummer, res: ArkivResultat, e: List<EttersendtVedlegg>) {
        søknader.getSøknadByEventidAndFnr(søknadId, fnr.fnr)?.let {
            it.registrerEttersending(fnr, res, e)
            it.avsluttMinSideOppgaveHvisKomplett(fnr)
        } ?: log.warn("Ingen tidligere innsendt søknad med id $søknadId ble funnet for $fnr (dette skal aldri skje)")
    }

    private fun fullførEttersendingUtenSøknad(fnr: Fødselsnummer, res: ArkivResultat, e: List<EttersendtVedlegg>) {
        søknader.sisteSøknad(fnr)?.let {
            log.trace("Knytter ettersending til siste søknad ${it.eventid} med journalpost ${it.journalpostid}")
            it.registrerEttersending(fnr, res, e)
        } ?: log.warn("Fant ingen sist innsendt søknad for $fnr")
        minside.opprettBeskjed(MINAAPSTD, callIdAsUUID(), fnr,
                "Vi har mottatt din ${STANDARD_ETTERSENDING.tittel.decapitalize()}", true)
    }

    private fun SøknadRepository.sisteSøknad(fnr: Fødselsnummer) =
        getSøknadByFnr(fnr.fnr, SISTE_SØKNAD).firstOrNull()

    private fun Søknad.oppdaterMinSide(erKomplett: Boolean, fnr: Fødselsnummer) =
        if (erKomplett) {
            minside.opprettBeskjed(MINAAPSTD, eventid, fnr, "Vi har mottatt din ${STANDARD.tittel.decapitalize()}", true)
        }
        else {
            minside.opprettOppgave(MINAAPSTD, fnr, eventid, "Vi har mottatt din ${STANDARD.tittel.decapitalize()}. Du må ettersende dokumentasjon")
        }

    private fun Søknad.avsluttMinSideOppgaveHvisKomplett(fnr: Fødselsnummer) {
        if (manglendevedlegg.isEmpty()) {
            minside.avsluttOppgave(STANDARD, fnr, eventid).also {
                log.trace("Alle manglende vedlegg er sendt inn, avsluttet oppgave $eventid")
            }
        }
        else {
            with(manglendevedlegg) {
                log.trace("Det mangler fremdeles $size vedlegg (${map { it.vedleggtype }})")
            }
        }
    }
}

private fun String.decapitalize()  = replaceFirstChar { it.lowercase(getDefault())}