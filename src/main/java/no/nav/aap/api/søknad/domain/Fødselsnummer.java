package no.nav.aap.api.søknad.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.constraints.NotNull;

import static no.nav.aap.api.søknad.util.StringUtil.partialMask;

public record Fødselsnummer(@NotNull @JsonValue String fnr) {

    @JsonValue(false)
    @Override
    public String fnr() {
        return fnr;
    }
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [fnr=" + partialMask(fnr) + "]";
    }
}
