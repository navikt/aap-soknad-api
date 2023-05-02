package no.nav.aap.api.søknad.fordeling

import java.util.UUID

data class Ettersending(val søknadId: UUID?,
                        val ettersendteVedlegg: List<EttersendtVedlegg>) {
    data class EttersendtVedlegg(val ettersending: Vedlegg, val vedleggType: VedleggType)
}