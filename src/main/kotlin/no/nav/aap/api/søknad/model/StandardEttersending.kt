package no.nav.aap.api.søknad.model

import java.util.UUID

data class StandardEttersending(val søknadId: UUID?,
                                val ettersendteVedlegg: List<EttersendtVedlegg>) {
    data class EttersendtVedlegg(val ettersending: Vedlegg, val vedleggType: VedleggType)
}