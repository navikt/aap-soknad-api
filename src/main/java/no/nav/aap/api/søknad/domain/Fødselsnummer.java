package no.nav.aap.api.søknad.domain;

import javax.validation.constraints.NotNull;

import static no.nav.aap.api.søknad.util.StringUtil.partialMask;

public record Fødselsnummer(@NotNull String fnr) {

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [fnr=" + partialMask(fnr) + "]";
    }
}
