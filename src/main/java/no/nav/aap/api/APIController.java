package no.nav.aap.api;

import no.nav.aap.api.config.AbstractRestConfig;
import no.nav.aap.api.util.TokenUtil;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.security.token.support.spring.ProtectedRestController;
import org.springframework.web.bind.annotation.GetMapping;


@ProtectedRestController(value = "/api", issuer = AbstractRestConfig.ISSUER)
public class APIController {

    private final TokenUtil tokenUtil;

    public APIController(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @GetMapping(path = "me")
    public String me () {
        return tokenUtil.getSubject() + " er autentisert";
    }

    @GetMapping(path = "open")
    @Unprotected
    public String open () {
        return "open";
    }
}
