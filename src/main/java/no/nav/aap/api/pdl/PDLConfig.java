package no.nav.aap.api.pdl;

import no.nav.aap.api.config.AbstractRestConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.net.URI;

@ConfigurationProperties(prefix = "pdl")
public class PDLConfig extends AbstractRestConfig {
    private static final String DEFAULT_BASE_URI = "http://pdl-api.pdl/graphql";
    private static final String DEFAULT_PING_PATH = "/";

    static final String NAVN_QUERY = "query-navn.graphql";

    @ConstructorBinding
    public PDLConfig(@DefaultValue(DEFAULT_PING_PATH) String pingPath,
                     @DefaultValue("true") boolean enabled,
                     @DefaultValue(DEFAULT_BASE_URI) URI baseUri) {
        super(baseUri, pingPath, enabled);
    }
}
