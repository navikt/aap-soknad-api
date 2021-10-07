package no.nav.aap.api.domain;

import static no.nav.aap.api.util.StringUtil.partialMask;

public record Fødselsnummer(String fnr) {

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [fnr=" + partialMask(fnr) + "]";
    }
}
