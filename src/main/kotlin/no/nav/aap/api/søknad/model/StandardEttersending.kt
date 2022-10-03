package no.nav.aap.api.søknad.model

import java.util.*

data class StandardEttersending(val søknadId: UUID?,
                                val ettersendteVedlegg: List<EttersendtVedlegg>) {
    data class EttersendtVedlegg(val ettersending: Vedlegg, val vedleggType: VedleggType)
}