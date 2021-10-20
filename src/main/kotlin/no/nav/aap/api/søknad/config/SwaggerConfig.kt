package no.nav.aap.api.søknad.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType.OAS_30
import springfox.documentation.spring.web.plugins.Docket

@Configuration
@Import(
    BeanValidatorPluginsConfiguration::class
)
open class SwaggerConfig {
    @Bean
    open fun api(): Docket {
        return Docket(OAS_30)
            .apiInfo(apiInfo())
            .protocols(setOf("http", "https"))
            .select()
            .apis(RequestHandlerSelectors.basePackage("no.nav.aap"))
            .paths(PathSelectors.any())
            .build()
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfo(
            "AAP Søknad api",
            "API for mottak av AAP-søknader",
            "1.0",
            null,
            Contact("AAP-devs", "http://www.nav.no", "aap-dev@nav.no"),
            "MIT",
            "https://github.com/navikt/aap-soeknad-api/blob/main/LICENSE.md",
            listOf()
        )
    }
}