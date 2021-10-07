package no.nav.aap.api.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import static no.nav.aap.api.util.StringUtil.partialMask;

public record Fødselsnummer(@JsonValue String fnr) {

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [fnr=" + partialMask(fnr) + "]";
    }
}
