package no.nav.aap.helloworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static no.nav.foreldrepenger.boot.conditionals.Cluster.profiler;

@SpringBootApplication
public class HelloWorldApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HelloWorldApplication.class)
                .profiles(profiler())
                .main(HelloWorldApplication.class)
                .run(args);

    }
}
