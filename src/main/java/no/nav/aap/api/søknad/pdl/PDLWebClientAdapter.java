package no.nav.aap.api.søknad.pdl;

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException;
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient;
import no.nav.aap.api.søknad.rest.AbstractWebClientAdapter;
import no.nav.aap.api.søknad.tokenx.AuthContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;

@Component
public class PDLWebClientAdapter extends AbstractWebClientAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(PDLWebClientAdapter.class);
    private static final String IDENT = "ident";
    private static final String NAVN_QUERY = "query-navn.graphql";

    private final GraphQLWebClient graphQLWebClient;
    private final AuthContext tokenUtil;
    private final PDLErrorHandler errorHandler;

    PDLWebClientAdapter(@Qualifier(PDLClientConfig.PDL_USER) GraphQLWebClient graphQLWebClient, @Qualifier(PDLClientConfig.PDL_USER) WebClient webClient, PDLConfig cfg, AuthContext tokenUtil, PDLErrorHandler errorHandler) {
        super(webClient,cfg);
        this.graphQLWebClient = graphQLWebClient;
        this.tokenUtil = tokenUtil;
        this.errorHandler = errorHandler;
    }

    public PDLNavn navn() {
        return navn(tokenUtil.getSubject());
    }

    private PDLNavn navn(String id) {
        return oppslag(() -> graphQLWebClient.post(NAVN_QUERY, idFra(id), PDLWrappedNavn.class).block(), "navn")
                .navn().stream()
                .findFirst()
                .orElse(null);
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
    @Override
    public void ping() {
        LOG.trace("Pinger {}", getBaseUri());
         webClient
                .options()
                .uri(getBaseUri())
                .accept(APPLICATION_JSON, TEXT_PLAIN)
                .retrieve()
                .onStatus(HttpStatus::isError, ClientResponse::createException)
                .toBodilessEntity()
                .block();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "graphQLWebClient=" + graphQLWebClient +
                ", tokenUtil=" + tokenUtil +
                ", errorHandler=" + errorHandler +
                ", webClient=" + webClient +
                ", cfg=" + cfg + "]";
    }
}


