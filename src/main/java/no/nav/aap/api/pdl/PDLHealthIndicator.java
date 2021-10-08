package no.nav.aap.api.pdl;

import no.nav.aap.api.health.AbstractPingableHealthIndicator;
import org.springframework.stereotype.Component;

    @Component
    public class PDLHealthIndicator extends AbstractPingableHealthIndicator {
        public PDLHealthIndicator(PDLConnection connection) {
            super(connection);
        }
}
