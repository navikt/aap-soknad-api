package no.nav.aap.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.util.UriComponentsBuilder.newInstance;


@ControllerAdvice
public class AAPExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

    private final URI  wonderwall;

    public AAPExceptionHandler(@Value("${wonderwall.url:http://set.me}") URI wonderwall ) {
        this.wonderwall = wonderwall;
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleUncaught(Exception e, HttpServletRequest req)  {
        LOG.warn("Fikk exception " + e.getClass().getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(newInstance()
                .scheme(wonderwall.getScheme())
                .host(wonderwall.getHost())
                .path("/oauth2/login").queryParam("redirect",req.getRequestURL()).build().toUri());
        return new ResponseEntity<>(headers, SEE_OTHER);
    }
}
