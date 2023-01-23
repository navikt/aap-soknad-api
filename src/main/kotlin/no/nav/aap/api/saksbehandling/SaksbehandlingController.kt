package no.nav.aap.api.saksbehandling


import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.oppslag.søknad.SøknadClient
import no.nav.aap.api.saksbehandling.SaksbehandlingController.Companion.SB_BASE
import no.nav.aap.api.søknad.model.VedleggType
import no.nav.aap.util.Constants.AAD
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@ProtectedRestController(value = [SB_BASE], issuer = AAD, claimMap = [])
class SaksbehandlingController(private val client: SøknadClient) {

    @PostMapping("vedlegg")
    fun vedlegg(@RequestBody e: VedleggEtterspørsel)  =
        client.etterspørrVedlegg(e)?.let {
        status(CREATED).build()
    } ?: notFound().build<Unit>()
    companion object {
        const val SB_BASE = "/sb"
    }
    data class VedleggEtterspørsel(val fnr: Fødselsnummer, val type: VedleggType)
}