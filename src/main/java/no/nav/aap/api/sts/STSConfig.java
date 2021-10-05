package no.nav.aap.api.sts;

import no.nav.aap.api.config.AbstractRestConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters.FormInserter;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.Duration;

import static com.nimbusds.oauth2.sdk.GrantType.CLIENT_CREDENTIALS;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@ConfigurationProperties(prefix = "sts")
public class STSConfig extends AbstractRestConfig {

    private static final String DEFAULT_BASE_URI = "http://must.be.set";
    private static final String GRANT_TYPE = "grant_type";
    private static final String DEFAULT_PATH = "/rest/v1/sts/token";
    private static final String DEFAULT_SLACK = "20s";
    private static final String PING_PATH = ".well-known/openid-configuration";
    private static final String SCOPE = "scope";
    private final String username;
    private final String password;
    private final Duration slack;
    private final String stsPath;

    @ConstructorBinding
    public STSConfig(@DefaultValue(DEFAULT_BASE_URI) URI baseUri,
                     @DefaultValue(DEFAULT_SLACK) Duration slack, String username, String password,
                     @DefaultValue(PING_PATH) String pingPath, @DefaultValue("true") boolean enabled,
                     @DefaultValue(DEFAULT_PATH) String stsPath) {
        super(baseUri, pingPath, enabled);
        this.stsPath = stsPath;
        this.slack = slack;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Duration getSlack() {
        return slack;
    }

    public String getStsPath() {
        return stsPath;
    }

    URI getStsURI(UriBuilder b) {
        return b.path(stsPath)
                .build();
    }

    FormInserter<String> stsBody() {
        var m = new LinkedMultiValueMap<String, String>();
        m.add(GRANT_TYPE, CLIENT_CREDENTIALS.getValue());
        m.add(SCOPE, "openid");
        return fromFormData(m);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[username=" + username + ", password=" + password + ", slack=" + slack
                + ", stsPath=" + stsPath + "]";
    }

}
