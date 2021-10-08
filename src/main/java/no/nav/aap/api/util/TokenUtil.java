package no.nav.aap.api.util;

import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import no.nav.aap.api.domain.Fødselsnummer;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static no.nav.aap.api.config.AbstractRestConfig.ISSUER;

@Component
public class TokenUtil {

    private static final String BEARER = AccessTokenType.BEARER.getValue() + " ";

    private final TokenValidationContextHolder ctxHolder;

    public TokenUtil(TokenValidationContextHolder ctxHolder) {
        this.ctxHolder = ctxHolder;
    }

    private String getSubject(String issuer) {
        return getClaim(issuer, "pid");
    }

    public String getSubject() {
        return getSubject(ISSUER);
    }

    public String getClaim(String issuer, String claim) {
        return Optional.ofNullable(claimSet(issuer))
                .map(c -> c.getStringClaim(claim))
                .orElse(null);
    }

    public static String bearerToken(String token) {
        return BEARER + token;
    }

    private JwtTokenClaims claimSet() {
        return claimSet(ISSUER);
    }

    private JwtTokenClaims claimSet(String issuer) {
        return Optional.ofNullable(context())
                .map(s -> s.getClaims(issuer))
                .orElse(null);
    }

    private TokenValidationContext context() {
        return Optional.ofNullable(ctxHolder.getTokenValidationContext())
                .orElse(null);
    }

    public boolean erAutentisert() {
        return getToken() != null;
    }

    public String getToken() {
        return getToken(ISSUER);
    }

    private String getToken(String issuer) {
        return Optional.ofNullable(context())
                .map(c -> c.getJwtToken(issuer))
                .filter(Objects::nonNull)
                .map(JwtToken::getTokenAsString)
                .orElse(null);
    }

    public JwtToken getJWTToken() {
        return getJWTToken(ISSUER);
    }

    private JwtToken getJWTToken(String issuer) {
        return ctxHolder.getTokenValidationContext().getJwtToken(issuer);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [ctxHolder=" + ctxHolder + "]";
    }

    public Fødselsnummer getFnr() {
        return Optional.ofNullable(getSubject())
                .map(Fødselsnummer::new).orElseThrow();
    }
}
