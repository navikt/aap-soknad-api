package no.nav.aap.api.pdl;

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException;

public interface PDLErrorHandler {
    <T> T handleError(GraphQLErrorsException e);
}
