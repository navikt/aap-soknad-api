package no.nav.aap.api;

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

import static no.nav.foreldrepenger.boot.conditionals.Cluster.profiler;

@SpringBootApplication
@EnableJwtTokenValidation
@EnableOAuth2Client(cacheEnabled = true)
@ConfigurationPropertiesScan
@EnableRetry
public class AAPApiApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(AAPApiApplication.class)
                .profiles(profiler())
                .main(AAPApiApplication.class)
                .run(args);

    }
}
