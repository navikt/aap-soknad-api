package no.nav.aap.helloworld;

import no.nav.security.token.support.core.exceptions.JwtTokenMissingException;
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;


@ControllerAdvice
public class AAPExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

    private final URI  wonderwall;

    public AAPExceptionHandler(@Value("${wonderwall.url:http://set.me}") URI wonderwall ) {
        this.wonderwall = wonderwall;

    }

    @ExceptionHandler
    public ResponseEntity<Object> handleUncaught(Exception e, HttpServletRequest req)  {
        HttpHeaders headers = new HttpHeaders();
        var  ny = UriComponentsBuilder.newInstance()
                .scheme(wonderwall.getScheme())
                .host(wonderwall.getHost())
                .path("/oauth2/login").queryParam("redirect",req.getRequestURL()).build().toUri();
        headers.setLocation(ny);
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }
}
