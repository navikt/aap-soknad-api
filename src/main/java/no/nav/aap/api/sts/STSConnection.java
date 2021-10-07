package no.nav.aap.api.sts;

import static no.nav.aap.api.pdl.PdlClientConfig.STS;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.Duration;

import no.nav.aap.api.rest.AbstractWebClientConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;


//@Component
public class STSConnection extends AbstractWebClientConnection {

    private static final Logger LOG = LoggerFactory.getLogger(STSConnection.class);
    private final STSConfig cfg;

    public STSConnection(@Qualifier(STS) WebClient webClient, STSConfig cfg) {
        super(webClient, cfg);
        this.cfg = cfg;
    }

    SystemToken refresh() {
        LOG.trace("Refresh av system token");
        var token = webClient
                .post()
                .uri(cfg::getStsURI)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_FORM_URLENCODED)
                .body(cfg.stsBody())
                .exchange()
                .block()
                .bodyToMono(SystemToken.class)
                .block();
        LOG.trace("Refresh av system token OK ({})", token.getExpiration());
        return token;
    }

    public Duration getSlack() {
        return cfg.getSlack();
    }

    @Override
    public String name() {
        return "STS";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[cfg=" + cfg + "]";
    }

}
