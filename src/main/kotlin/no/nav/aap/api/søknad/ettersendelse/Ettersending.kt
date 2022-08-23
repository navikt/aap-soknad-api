package no.nav.aap.api.søknad.ettersendelse

import no.nav.aap.api.søknad.model.VedleggType
import java.util.*

data class Ettersending(val søknadId: UUID, val vedlegg: List<EttersendtVedlegg>) {
    data class EttersendtVedlegg(val uuid: UUID, val type: VedleggType)
}