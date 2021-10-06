package no.nav.aap.api.pdl;

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException;
import no.nav.aap.api.domain.Navn;
import no.nav.aap.api.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static no.nav.aap.api.pdl.PdlClientConfig.PDL_USER;
import static no.nav.aap.api.util.StreamUtil.safeStream;

@Component
public class PDLConnection {
    private static final Logger LOG = LoggerFactory.getLogger(PDLConnection.class);
    private static final String IDENT = "ident";
    private static final String NAVN_QUERY = "query-navn.graphql";

    private final GraphQLWebClient userClient;
    private final PDLConfig cfg;
    private final TokenUtil tokenUtil;
    private final PDLErrorResponseHandler errorHandler;

    PDLConnection(@Qualifier(PDL_USER) GraphQLWebClient userClient,
                  PDLConfig cfg, TokenUtil tokenUtil, PDLErrorResponseHandler errorHandler) {
        this.userClient = userClient;
        this.cfg = cfg;
        this.tokenUtil = tokenUtil;
        this.errorHandler = errorHandler;
    }

    public PDLNavn hentNavn() {
        return oppslagNavn(tokenUtil.getSubject());
    }

    public PDLNavn oppslagNavn(String id) {
        return oppslag(() -> userClient.post(NAVN_QUERY, idFra(id), PDLWrappedNavn.class).block(), "navn")
                .navn().stream().findFirst().orElse(null);
    }
    private static Map<String, Object> idFra(String id) {
        return Map.of(IDENT, id);
    }
    private <T> T oppslag(Supplier<T> oppslag, String type) {
        try {
            LOG.info("PDL oppslag {}", type);
            var res = oppslag.get();
            LOG.trace("PDL oppslag {} respons={}", type, res);
            LOG.info("PDL oppslag {} OK", type);
            return res;
        } catch (GraphQLErrorsException e) {
            LOG.warn("PDL oppslag {} feilet", type, e);
            return errorHandler.handleError(e);
        } catch (Exception e) {
            LOG.warn("PDL oppslag {} feilet med uventet feil", type, e);
            throw e;
        }
    }
}


