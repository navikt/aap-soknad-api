package no.nav.aap.helloworld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.regex.Pattern;

@Configuration
public class Configs {

    private static final Logger LOG = LoggerFactory.getLogger(Configs.class);

    @Bean
    public TokenXConfigFinder configFinder() {
        return (cfgs, req) -> {
            LOG.trace("Oppslag token X konfig for {}", req.getHost());
            var cfg = cfgs.getRegistration().get(hostNavnUtenNamespace(req));
            if (cfg != null) {
                LOG.trace("Oppslag token X konfig for {} OK", req.getHost());
            } else {
                LOG.trace("Oppslag token X konfig for {} fant ingenting", req.getHost());
            }
            return cfg;
        };
    }

    private static String hostNavnUtenNamespace(URI req) {
        return req.getHost().split(Pattern.quote("."))[0];
    }
}
