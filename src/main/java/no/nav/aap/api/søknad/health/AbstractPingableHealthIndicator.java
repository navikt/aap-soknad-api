package no.nav.aap.api.søknad.health;

import no.nav.aap.api.søknad.rest.PingEndpointAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public abstract class AbstractPingableHealthIndicator implements HealthIndicator {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPingableHealthIndicator.class);

    private final PingEndpointAware pingable;

    public AbstractPingableHealthIndicator(PingEndpointAware pingable) {
        this.pingable = pingable;
    }

    @Override
    public Health health() {
        try {
            LOG.trace("Pinger {} på {}", pingable.name(), pingable.pingEndpoint());
            var respons = pingable.ping();
            LOG.trace("Ping {} fikk respons {}", pingable.name(), respons);
            return up();
        } catch (Exception e) {
            return down(e);
        }
    }

    private Health up() {
        return Health.up()
                .withDetail(pingable.name(), pingable.pingEndpoint())
                .build();
    }

    private Health down(Exception e) {
        LOG.warn("Kunne ikke pinge {} på {}", pingable.name(), pingable.pingEndpoint(), e);
        return Health.down()
                .withDetail(pingable.name(), pingable.pingEndpoint())
                .withException(e)
                .build();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [pingable=" + pingable + "]";
    }
}
