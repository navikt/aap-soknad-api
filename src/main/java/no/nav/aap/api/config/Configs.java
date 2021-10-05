package no.nav.aap.api.config;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher;
import no.nav.security.token.support.client.spring.oauth2.OAuth2ClientRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.regex.Pattern;

@Configuration
public class Configs {

    public static final String IDPORTEN = "idporten";

    private static final Logger LOG = LoggerFactory.getLogger(Configs.class);

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
