package no.nav.aap.api.pdl;


import java.util.Set;

record PDLWrappedNavn(Set<PDLNavn> navn) {
}
record PDLNavn(String fornavn, String mellomnavn, String etternavn) {
}
