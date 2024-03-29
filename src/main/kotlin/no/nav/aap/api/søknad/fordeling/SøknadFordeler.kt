package no.nav.aap.api.søknad.fordeling

import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.UUID
import org.springframework.stereotype.Component
import no.nav.aap.api.oppslag.person.PDLClient
import no.nav.aap.api.søknad.arkiv.ArkivFordeler
import no.nav.aap.api.søknad.fordeling.SøknadFordeler.Kvittering
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil

interface Fordeler {

    fun fordel(innsending : Innsending) : Kvittering
    fun fordel(e : Ettersending) : Kvittering
}

@Component
class SøknadFordeler(private val arkiv : ArkivFordeler,
                     private val pdl : PDLClient,
                     private val repo : SøknadRepository,
                     private val fullfører : SøknadFullfører,
                     private val ctx : AuthContext,
                     private val cfg : VLFordelingConfig,
                     private val vlFordeler : SøknadVLFordeler) : Fordeler {

    private val log = LoggerUtil.getLogger(SøknadFordeler::class.java)

    override fun fordel(innsending : Innsending) =
        pdl.søkerMedBarn().run {
            with(arkiv.fordel(innsending, this)) {
                innsending.søknad.fødselsdato = fødseldato
                vlFordeler.fordel(innsending.søknad, fnr, journalpostId, cfg.standard)
                fullfører.fullfør(fnr, innsending.søknad, this)
            }
        }

    override fun fordel(e : Ettersending) =
        pdl.søkerUtenBarn().run {
            with(arkiv.fordel(e, this, e.tilVikafossen())) {
                vlFordeler.fordel(e, fnr, journalpostId, cfg.ettersending)
                fullfører.fullfør(fnr, e, this)
            }
        }

    private fun Ettersending.tilVikafossen() = søknadId?.let {
        repo.getSøknadByEventidAndFnr(it, ctx.getFnr().fnr)?.routing
    } ?: false

    data class Kvittering(val journalpostId : String = "0", val tidspunkt : LocalDateTime = now(), val uuid : UUID? = null)
}