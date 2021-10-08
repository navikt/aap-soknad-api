package no.nav.aap.api.tokenx;

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
}
