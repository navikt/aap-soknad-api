package no.nav.aap.api.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import static no.nav.aap.api.util.StringUtil.*;

public class Fødselsnummer {

        @JsonValue
        private final String fnr;

        public Fødselsnummer(String fnr) {
            this.fnr = fnr;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " [fnr=" + partialMask(fnr) + "]";
        }
    }
