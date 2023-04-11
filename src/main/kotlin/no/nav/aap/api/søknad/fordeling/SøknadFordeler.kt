package no.nav.aap.api.søknad.fordeling

import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.UUID
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.person.PDLClient
import no.nav.aap.api.søknad.arkiv.ArkivFordeler
import no.nav.aap.api.søknad.fordeling.SøknadFordeler.Kvittering
import no.nav.aap.api.søknad.model.Innsending
import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.UtlandSøknad

interface Fordeler {
    fun fordel(søknad: UtlandSøknad): Kvittering
    fun fordel(innsending: Innsending): Kvittering
    fun fordel(e: StandardEttersending): Kvittering

}

@Component
class SøknadFordeler(private val arkiv: ArkivFordeler,
                     private val pdl: PDLClient,
                     private val fullfører: SøknadFullfører,
                     private val cfg: VLFordelingConfig,
                     private val vlFordeler: SøknadVLFordeler) : Fordeler {

    override fun fordel(innsending: Innsending) =
        pdl.søkerMedBarn().run {
            with(arkiv.fordel(innsending, this)) {
                innsending.søknad.fødselsdato = fødseldato
                vlFordeler.fordel(innsending.søknad, fnr, journalpostId, cfg.standard)
                fullfører.fullfør(fnr, innsending.søknad, this)
            }
        }

    override fun fordel(e: StandardEttersending) =
    pdl.søkerUtenBarn().run {
        with(arkiv.fordel(e, this)) {
            vlFordeler.fordel(e, fnr, journalpostId, cfg.ettersending)
            fullfører.fullfør(fnr, e, this)
        }
    }

    override fun fordel(søknad: UtlandSøknad) =
        pdl.søkerUtenBarn().run {
            with(arkiv.fordel(søknad, this)) {
                vlFordeler.fordel(søknad, fnr, journalpostId, cfg.utland)
                fullfører.fullfør(fnr, søknad, this)
            }
        }

    data class Kvittering(val journalpostId: String = "0", val tidspunkt: LocalDateTime = now(),val uuid: UUID? = null)
}