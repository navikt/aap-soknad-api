package no.nav.aap.helloworld;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static no.nav.foreldrepenger.boot.conditionals.Cluster.profiler;

@SpringBootApplication
@EnableJwtTokenValidation
public class HelloWorldApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HelloWorldApplication.class)
                .profiles(profiler())
                .main(HelloWorldApplication.class)
                .run(args);

    }
}
