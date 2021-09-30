package no.nav.aap.helloworld;

import no.nav.security.token.support.core.api.Unprotected;
import no.nav.security.token.support.spring.ProtectedRestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@ProtectedRestController(value = "/api", issuer = "idporten")
public class IDPortenController {


    @GetMapping(path = "me")
    public String me () {
        return "hello";
    }

    @GetMapping(path = "open")
    @Unprotected
    public String open () {
        return "open";
    }
}
