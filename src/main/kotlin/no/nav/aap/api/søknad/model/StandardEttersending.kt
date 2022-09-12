package no.nav.aap.api.søknad.model

import java.util.*
import javax.validation.constraints.NotEmpty

data class StandardEttersending(val søknadId: UUID?,
                                val ettersendteVedlegg: List<EttersendtVedlegg>) {
    data class EttersendtVedlegg(val ettersending: Vedlegg, @NotEmpty val vedleggType: VedleggType)
}