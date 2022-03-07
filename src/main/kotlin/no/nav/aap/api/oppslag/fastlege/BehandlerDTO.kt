package no.nav.aap.api.oppslag.fastlege

import no.nav.aap.api.felles.Navn

data class BehandlerDTO(
        val type: String,
        val behandlerRef: String,
        val fnr: String?,
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val orgnummer: String?,
        val kontor: String?,
        val adresse: String?,
        val postnummer: String?,
        val poststed: String?,
        val telefon: String?)

data class Fastlege(val navn: Navn?) // TODO mer