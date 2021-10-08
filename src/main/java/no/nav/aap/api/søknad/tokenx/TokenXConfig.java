package no.nav.aap.api.søknad.tokenx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
 class TokenXConfig {
    @Bean
     TokenXConfigMatcher configMatcher() {
        return (cfgs, req) -> {
            return Optional.ofNullable(cfgs.getRegistration().get(req.getHost().split("\\.")[0]));
        };
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return b -> b.mixIn(OAuth2AccessTokenResponse.class, IgnoreUnknownMixin.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private interface IgnoreUnknownMixin {
    }
}
