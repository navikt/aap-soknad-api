package no.nav.aap.api.pdl;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher;
import no.nav.security.token.support.client.spring.oauth2.OAuth2ClientRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PdlClientConfig {

    static final String NAVN_QUERY = "query-navn.graphql";

    private static final Logger LOG = LoggerFactory.getLogger(PdlClientConfig.class);

    @Bean
    public ClientConfigurationPropertiesMatcher matcher()  {
        return new ClientConfigurationPropertiesMatcher() {
        };
   }

    @Bean
    public OAuth2ClientRequestInterceptor oauthInterceptor(ClientConfigurationProperties properties,OAuth2AccessTokenService service, ClientConfigurationPropertiesMatcher matcher) {
      return new OAuth2ClientRequestInterceptor( properties,service, matcher) ;
    }
}
