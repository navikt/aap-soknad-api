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
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class AAPExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

    @ExceptionHandler
    public ResponseEntity<Object> handleUncaught(Exception e, HttpHeaders headers, WebRequest req) {

        LOG.warn("XXXXXX {} {}", req.getContextPath(), e.getClass().getSimpleName());
        return handleExceptionInternal(e,"oops",headers ,HttpStatus.UNAUTHORIZED,req);
    }
}
