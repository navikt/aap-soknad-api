package no.nav.aap.api.søknad.pdl;


import java.util.Set;

record PDLWrappedNavn(Set<PDLNavn> navn) {
}
record PDLNavn(String fornavn, String mellomnavn, String etternavn) {
}
