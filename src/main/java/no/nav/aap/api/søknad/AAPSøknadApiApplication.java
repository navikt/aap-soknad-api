package no.nav.aap.api.søknad;

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

import static no.nav.foreldrepenger.boot.conditionals.Cluster.profiler;

@SpringBootApplication
@EnableJwtTokenValidation(ignore  = {"springfox.documentation","org.springframework" })
@EnableOAuth2Client(cacheEnabled = true)
@ConfigurationPropertiesScan
@EnableRetry
public class AAPSøknadApiApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(AAPSøknadApiApplication.class)
                .profiles(profiler())
                .main(AAPSøknadApiApplication.class)
                .run(args);

    }
}
