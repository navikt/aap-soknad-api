package no.nav.aap.helloworld;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api")
public class IDPortenController {


    @GetMapping(path = "me")
    public String me () {
        return "hello";
    }


}
