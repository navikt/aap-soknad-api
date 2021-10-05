package no.nav.aap.helloworld;

import no.nav.security.token.support.core.api.Unprotected;
import no.nav.security.token.support.spring.ProtectedRestController;
import org.springframework.web.bind.annotation.GetMapping;

@ProtectedRestController(value = "/api", issuer = "idporten")
public class IDPortenController {

    private final TokenUtil tokenUtil;

    public IDPortenController(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @GetMapping(path = "me")
    public String me () {
        return tokenUtil.getToken("idporten") + " er autentisert";
    }

    @GetMapping(path = "open")
    @Unprotected
    public String open () {
        return "open";
    }
}
