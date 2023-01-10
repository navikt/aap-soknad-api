package no.nav.aap.api.saksbehandling


import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.søknad.SøknadClient
import no.nav.aap.api.saksbehandling.SaksbehandlingController.Companion.SB_BASE
import no.nav.aap.api.søknad.minside.MinSideClient
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.util.Constants.AAD
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus

@ProtectedRestController(value = [SB_BASE], issuer = AAD, claimMap = [])
class SaksbehandlingController(private val client: SøknadClient, private val minside: MinSideClient) {

    @PostMapping("vedlegg")
    @ResponseStatus(CREATED)
    fun vedlegg(@RequestBody e: VedleggEtterspørsel) : Unit {
        client.etterspørrVedlegg(e)
       // minside.opprettOppgave(e.fnr.fnr,)
    }
    companion object {
        const val SB_BASE = "/sb"
    }
    data class VedleggEtterspørsel(val fnr: Fødselsnummer, val type: VedleggType)
}