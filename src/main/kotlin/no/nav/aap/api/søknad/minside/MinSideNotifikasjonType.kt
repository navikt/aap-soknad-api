package no.nav.aap.api.søknad.minside

import java.util.UUID
import org.springframework.web.util.UriComponentsBuilder
import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.felles.SkjemaType.STANDARD
import no.nav.aap.api.felles.SkjemaType.UTLAND_SØKNAD
import no.nav.aap.api.søknad.minside.MinSideConfig.BacklinksConfig
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.MinSideBacklinkContext.MINAAP
import no.nav.aap.api.søknad.minside.MinSideNotifikasjonType.MinSideBacklinkContext.SØKNAD

data class MinSideNotifikasjonType private constructor(private val skjemaType: SkjemaType, private val ctx: MinSideBacklinkContext) {

    private enum class MinSideBacklinkContext { MINAAP, SØKNAD }

    fun link(cfg: BacklinksConfig, eventId: UUID? = null) =
        when (skjemaType) {
            STANDARD -> when (ctx) {
                MINAAP -> eventId?.let { UriComponentsBuilder.fromUri(cfg.innsyn)
                    .queryParam("eventId", it).build().toUri() }
                    ?: cfg.innsyn
                SØKNAD -> cfg.standard
            }
            UTLAND_SØKNAD -> when (ctx) {
                MINAAP -> cfg.innsyn
                SØKNAD -> cfg.utland
            }
            else -> null
        }

    companion object {
        val MINAAPSTD = MinSideNotifikasjonType(STANDARD, MINAAP)
        val SØKNADSTD = MinSideNotifikasjonType(STANDARD, SØKNAD)
    }
     enum class NotifikasjonType  { OPPGAVE,BESKJED }
}