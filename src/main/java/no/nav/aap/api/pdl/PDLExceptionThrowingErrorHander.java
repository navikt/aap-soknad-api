package no.nav.aap.api.pdl;
import graphql.kickstart.spring.webclient.boot.GraphQLError;
import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Objects;

import static no.nav.aap.api.util.StreamUtil.safeStream;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.web.client.HttpClientErrorException.create;

@Component
public class PDLExceptionThrowingErrorHander implements PDLErrorHandler {
    private static final String UAUTENTISERT = "unauthenticated";
    private static final String FORBUDT = "unauthorized";
    private static final String UGYLDIG = "bad_request";
    private static final String IKKEFUNNET = "not_found";
    private static final Logger LOG = LoggerFactory.getLogger(PDLExceptionThrowingErrorHander.class);

    @Override
    public <T> T handleError(GraphQLErrorsException e) {
        LOG.warn("PDL oppslag returnerte {} feil. {}", e.getErrors().size(), e.getErrors(), e);
        throw safeStream(e.getErrors())
                .findFirst()
                .map(GraphQLError::getExtensions)
                .map(m -> m.get("code"))
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .map(k -> exceptionFra(k, e.getMessage()))
                .orElse(new HttpServerErrorException(INTERNAL_SERVER_ERROR, e.getMessage(), null, null, null));
    }

    private static HttpStatusCodeException exceptionFra(String kode, String msg) {
        return switch (kode) {
            case UAUTENTISERT -> exception(UNAUTHORIZED, msg);
            case FORBUDT -> exception(FORBIDDEN, msg);
            case UGYLDIG -> exception(BAD_REQUEST, msg);
            case IKKEFUNNET -> exception(NOT_FOUND, msg);
            default -> new HttpServerErrorException(INTERNAL_SERVER_ERROR, msg);
        };
    }

    static HttpStatusCodeException exception(HttpStatus status, String msg) {
        return create(status, msg, null, null, null);
    }
}
