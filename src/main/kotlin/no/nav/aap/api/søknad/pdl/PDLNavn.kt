package no.nav.aap.api.søknad.pdl

internal data class PDLWrappedNavn(val navn: Set<PDLNavn>)
internal data class PDLNavn(val fornavn: String,val mellomnavn: String,val etternavn: String)
