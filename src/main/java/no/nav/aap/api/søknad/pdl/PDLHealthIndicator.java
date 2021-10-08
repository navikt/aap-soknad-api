package no.nav.aap.api.søknad.pdl;

import no.nav.aap.api.søknad.health.AbstractPingableHealthIndicator;
import org.springframework.stereotype.Component;

    @Component
    public class PDLHealthIndicator extends AbstractPingableHealthIndicator {
        public PDLHealthIndicator(PDLWebClientAdapter adapter) {
            super(adapter);
        }
}
