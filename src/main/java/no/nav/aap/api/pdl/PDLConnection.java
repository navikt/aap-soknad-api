package no.nav.aap.api.pdl;

import no.nav.aap.api.util.TokenUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException;
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient;

import static no.nav.aap.api.pdl.PdlClientConfig.PDL_SYSTEM;
import static no.nav.aap.api.pdl.PdlClientConfig.PDL_USER;

@Component
public class PDLConnection {

    private final GraphQLWebClient userClient;
    //private final GraphQLWebClient systemClient;
    private final PDLConfig cfg;
    private final TokenUtil tokenUtil;
    private final PDLErrorResponseHandler errorHandler;

    PDLConnection(@Qualifier(PDL_USER) GraphQLWebClient userClient,
                  //@Qualifier(PDL_SYSTEM) GraphQLWebClient systemClient,
                  PDLConfig cfg, TokenUtil tokenUtil, PDLErrorResponseHandler errorHandler) {
        this.userClient = userClient;
        //this.systemClient = systemClient;
        this.cfg = cfg;
        this.tokenUtil = tokenUtil;
        this.errorHandler = errorHandler;
    }
}


