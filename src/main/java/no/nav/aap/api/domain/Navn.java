package no.nav.aap.api.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "fornavn","mellomnavn","etternavn"})
public record Navn(String fornavn, String mellomnavn, String etternavn) {
}