package no.nav.aap.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Set;

import static springfox.documentation.spi.DocumentationType.OAS_30;

@Configuration
@EnableOpenApi
public class SpringFoxConfig {
    @Bean
    public Docket api() {
        return new Docket(OAS_30)
                .protocols(Set.of("http", "https"))
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
}
