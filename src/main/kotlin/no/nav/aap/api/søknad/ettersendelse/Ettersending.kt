package no.nav.aap.api.søknad.ettersendelse

import no.nav.aap.api.søknad.model.Vedlegg
import no.nav.aap.api.søknad.model.VedleggType
import java.util.*

data class Ettersending(val søknadId: UUID,
                        val vedlegg: List<EttersendtVedlegg>) {
    data class EttersendtVedlegg(val vedlegg: Vedlegg, val type: VedleggType)
}