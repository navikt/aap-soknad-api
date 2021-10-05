package no.nav.aap.helloworld;

import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class TokenUtil {

    public static final String BEARER = "Bearer ";

    private final TokenValidationContextHolder ctxHolder;

    public TokenUtil(TokenValidationContextHolder ctxHolder) {
        this.ctxHolder = ctxHolder;
    }

    public String getSubject(String issuer) {
        return Optional.ofNullable(claimSet(issuer))
                .map(JwtTokenClaims::getStringClaim("pid"))
                .orElse(null);
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

    public String getToken(String issuer) {
        return Optional.ofNullable(context())
                .map(c -> c.getJwtToken(issuer))
                .filter(Objects::nonNull)
                .map(JwtToken::getTokenAsString)
                .orElse(null);
    }

    public JwtToken getJWTToken(String issuer) {
        return ctxHolder.getTokenValidationContext().getJwtToken(issuer);

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [ctxHolder=" + ctxHolder + "]";
    }

}
