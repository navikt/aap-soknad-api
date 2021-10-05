package no.nav.aap.api;

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static no.nav.foreldrepenger.boot.conditionals.Cluster.profiler;

@SpringBootApplication
@EnableJwtTokenValidation
@EnableOAuth2Client(cacheEnabled = true)
public class AAPApiApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(AAPApiApplication.class)
                .profiles(profiler())
                .main(AAPApiApplication.class)
                .run(args);

    }
}
