package no.nav.aap.helloworld;

import no.nav.security.token.support.core.exceptions.JwtTokenMissingException;
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;


@ControllerAdvice
public class AAPExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

    @ExceptionHandler
    public ResponseEntity<Object> handleUncaught(Exception e, WebRequest req)  {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://www.vg.no"));
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }
}
