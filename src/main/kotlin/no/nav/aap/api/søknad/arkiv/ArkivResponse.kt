package no.nav.aap.api.søknad.arkiv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


data class DokumentInfoId(val dokumentInfoId: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArkivResponse(val journalpostId: String,
                         val journalpostferdigstilt: Boolean,
                         val dokumenter: List<DokumentInfoId>)