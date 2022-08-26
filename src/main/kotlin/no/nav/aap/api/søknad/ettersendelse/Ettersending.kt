package no.nav.aap.api.søknad.ettersendelse

import no.nav.aap.api.søknad.model.Vedlegg
import no.nav.aap.api.søknad.model.VedleggType
import java.util.*

data class Ettersending(val søknadId: UUID,
                        val ettersendteVedlegg: List<EttersendtVedlegg>) {
    data class EttersendtVedlegg(val ettersending: Vedlegg, val type: VedleggType)
}