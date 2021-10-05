package no.nav.aap.api.sts;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.security.token.support.core.jwt.JwtToken;

import java.time.Duration;
import java.time.LocalDateTime;

import static no.nav.aap.api.util.StringUtil.limit;
import static no.nav.aap.api.util.TimeUtil.localDateTime;

public record SystemToken(@JsonProperty("access_token") JwtToken accessToken,
                          @JsonProperty("expires_in") Long expiresIn,
                          @JsonProperty("token_type") String tokenType,
                          @JsonProperty("scope") String scope) {

    boolean isExpired(Duration slack) {
        return LocalDateTime.now().isAfter(getExpiration().minus(slack));
    }

    String getToken() {
        return accessToken().getTokenAsString();
    }

    LocalDateTime getExpiration() {
        return localDateTime(accessToken().getJwtTokenClaims().getExpirationTime());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[accessToken=" + limit(accessToken.getTokenAsString(), 12)
                + ", expires=" + getExpiration()
                + ", scope=" + scope
                + ", tokenType=" + tokenType + "]";
    }

}
