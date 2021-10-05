package no.nav.aap.api.pdl;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient;
import no.nav.aap.api.sts.STSConfig;
import no.nav.aap.api.sts.SystemTokenTjeneste;
import no.nav.aap.api.util.MDCUtil;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher;
import no.nav.security.token.support.client.spring.oauth2.OAuth2ClientRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import java.net.URI;
import java.util.Optional;

import static no.nav.aap.api.util.MDCUtil.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static no.nav.aap.api.util.TokenUtil.BEARER;
@Configuration
public class PdlClientConfig {

    static final String NAVN_QUERY = "query-navn.graphql";

    private static final Logger LOG = LoggerFactory.getLogger(PdlClientConfig.class);
    public static final String PDL_USER = "PDL";
    public static final String PDL_SYSTEM = "PDL_SYSTEM";
    public static final String STS = "STS";
    private static final String TEMA = "TEMA";
    private static final String AAP = "AAP";
    private static final String NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";

    @Value("${spring.application.name:aap-api}")
    private String consumer;

    private String consumerId() {
        return Optional.ofNullable(MDCUtil.consumerId())
                .orElse(consumer);
    }

    private static ExchangeFilterFunction pdlSystemUserExchangeFilterFunction(SystemTokenTjeneste sts) {
        return (req, next) -> next.exchange(ClientRequest.from(req)
                .header(AUTHORIZATION, sts.bearerToken())
                .header(TEMA, AAP)
                .header(NAV_CONSUMER_TOKEN, sts.bearerToken())
                .build());
    }

    private ExchangeFilterFunction correlatingFilterFunction() {
        return (req, next) -> next.exchange(ClientRequest.from(req)
                .header(NAV_CONSUMER_ID, consumerId())
                .header(NAV_CALL_ID, MDCUtil.callId())
                .header(NAV_CALL_ID1, MDCUtil.callId())
                .build());
    }

    private static ExchangeFilterFunction temaFilterFunction() {
        return (req, next) -> next.exchange(ClientRequest.from(req)
                .header(TEMA, AAP)
                .build());
    }

    @Bean
    public ClientConfigurationPropertiesMatcher matcher()  {
        return new ClientConfigurationPropertiesMatcher() {
        };
   }

    @Bean
    public OAuth2ClientRequestInterceptor oauthInterceptor(ClientConfigurationProperties properties,OAuth2AccessTokenService service, ClientConfigurationPropertiesMatcher matcher) {
      return new OAuth2ClientRequestInterceptor( properties,service, matcher) ;
    }

    //@Bean
    @Qualifier(STS)
    public WebClient webClientSTS(Builder builder, STSConfig cfg) {
        return builder
                .baseUrl(cfg.getBaseUri().toString())
                .filter(correlatingFilterFunction())
                .defaultHeaders(h -> h.setBasicAuth(cfg.getUsername(), cfg.getPassword()))
                .build();
    }

    @Qualifier(PDL_USER)
    @Bean
    public WebClient webClientPDL(Builder builder, PDLConfig cfg, TokenXFilterFunction tokenXFilterFunction) {
        return builder
                .baseUrl(cfg.getBaseUri().toString())
                .filter(correlatingFilterFunction())
                .filter(temaFilterFunction())
                .filter(tokenXFilterFunction)
                .build();
    }

    @Qualifier(PDL_SYSTEM)
   // @Bean
    public WebClient webClientSystemPDL(Builder builder, PDLConfig cfg, SystemTokenTjeneste sts) {
        return builder
                .baseUrl(cfg.getBaseUri().toString())
                .filter(correlatingFilterFunction())
                .filter(pdlSystemUserExchangeFilterFunction(sts))
                .build();
    }

    @Qualifier(PDL_USER)
    @Bean
    public GraphQLWebClient pdlWebClient(@Qualifier(PDL_USER) WebClient client, ObjectMapper mapper) {
        return GraphQLWebClient.newInstance(client, mapper);
    }

    @Qualifier(PDL_SYSTEM)
    @Bean
    public GraphQLWebClient pdlSystemWebClient(@Qualifier(PDL_SYSTEM) WebClient client, ObjectMapper mapper) {
        return GraphQLWebClient.newInstance(client, mapper);
    }

    @Component
    public class TokenXFilterFunction implements ExchangeFilterFunction {

        private static final Logger LOG = LoggerFactory.getLogger(TokenXFilterFunction.class);

        private final OAuth2AccessTokenService service;
        private final ClientConfigurationPropertiesMatcher matcher;
        private final ClientConfigurationProperties configs;

        TokenXFilterFunction(ClientConfigurationProperties configs, OAuth2AccessTokenService service, ClientConfigurationPropertiesMatcher matcher) {
            this.service = service;
            this.matcher = matcher;
            this.configs = configs;
        }

        @Override
        public Mono<ClientResponse> filter(ClientRequest req, ExchangeFunction next) {
            var url = req.url();
            LOG.trace("Sjekker token exchange for {}", url);
            var vanilla = new HttpRequest() {
                @Override
                public HttpHeaders getHeaders() {
                    return new HttpHeaders();
                }

                @Override
                public URI getURI() {
                    return req.url();
                }
                @Override
                public String getMethodValue() {
                    return HttpMethod.POST.name();
                }
            };
            var config = matcher.findProperties(configs, vanilla);
            if (config.isPresent()) {
                LOG.trace("Gjør token exchange for {} med konfig {}", url, config.get());
                var token = service.getAccessToken(config.get()).getAccessToken();
                LOG.info("Token exchange for {} OK", url);
                return next
                        .exchange(ClientRequest.from(req).header(AUTHORIZATION, BEARER + token)
                        .build());
            }
            LOG.trace("Ingen token exchange for {}", url);
            return next.exchange(ClientRequest.from(req).build());
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " [service=" + service + ", matcher=" + matcher + ", configs=" + configs + "]";
        }
    }
}
