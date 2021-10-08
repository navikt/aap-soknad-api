package no.nav.aap.api.pdl;

import no.nav.aap.api.health.AbstractPingableHealthIndicator;
import org.springframework.stereotype.Component;

public class PDLHealthIndicator {
    @Component
    public class OppslagHealthIndicator extends AbstractPingableHealthIndicator {
        public OppslagHealthIndicator(PDLConnection connection) {
            super(connection);
        }
    }
}
