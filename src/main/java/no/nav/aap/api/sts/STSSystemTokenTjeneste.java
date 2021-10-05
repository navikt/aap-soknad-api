package no.nav.aap.api.sts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class STSSystemTokenTjeneste implements SystemTokenTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(STSSystemTokenTjeneste.class);
    private final STSConnection connection;
    private SystemToken systemToken;

    public STSSystemTokenTjeneste(STSConnection connection) {
        this.connection = connection;
        LOG.info("System token {}", connection.refresh());
    }

    @Override
    public SystemToken getSystemToken() {
        if (systemToken == null || systemToken.isExpired(connection.getSlack())) {
            systemToken = connection.refresh();
        }
        return systemToken;
    }

    @Override
    public String ping() {
        return connection.ping();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[connection=" + connection + ", systemToken=" + systemToken + "]";
    }

}
