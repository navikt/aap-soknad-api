package no.nav.aap.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;
import java.util.Set;

import static springfox.documentation.builders.PathSelectors.any;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.OAS_30;

@Configuration
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(OAS_30)
                .apiInfo(apiInfo())
                .protocols(Set.of("http", "https"))
                .select()
                .apis(basePackage("no.nav.aap"))
                .paths(any())
                .build();
    }
    private ApiInfo apiInfo() {
        return new ApiInfo(
                "AAP Søknad api",
                "API for mottak av AAP-søknader",
                "1.0",
                null,
                new Contact("AAP-devs", "http://www.nav.no", "aap-dev@nav.no"),
                "MIT",
                "https://github.com/navikt/aap-soeknad-api/blob/main/LICENSE.md",
                List.of());
    }
}
