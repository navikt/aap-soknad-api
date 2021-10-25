package no.nav.aap.api.rest.sts;

import no.nav.aap.api.rest.AbstractRestConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import static no.nav.aap.api.config.Constants.STS;

@Configuration
public class STSClientConfig {

    @Bean
    @Qualifier(STS)
    public WebClient webClientSTS(WebClient.Builder builder, STSConfig cfg) {
        return builder
                .baseUrl(cfg.getBaseUri().toString())
                .filter(AbstractRestConfig.Companion.correlatingFilterFunction())
                .defaultHeaders(h -> h.setBasicAuth(cfg.getUsername(), cfg.getPassword()))
                .build();
    }
}
