package no.nav.aap.api.søknad.domain;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
public record Søker(@JsonUnwrapped Fødselsnummer fnr,Navn navn) {
}
