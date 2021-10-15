package no.nav.aap.api.søknad.error

import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.SEE_OTHER
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class AAPApiExceptionHandler(@param:Value("\${wonderwall.uri}") private val wonderwall: URI) :
    ResponseEntityExceptionHandler() {
    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun handleMissingOrExpiredToken(e: Exception?, req: HttpServletRequest): ResponseEntity<Any> {
        val headers = HttpHeaders()
        headers.location = UriComponentsBuilder.newInstance()
            .scheme(wonderwall.scheme)
            .host(wonderwall.host)
            .path("/oauth2/login").queryParam("redirect", req.requestURL).build().toUri()
        return ResponseEntity(headers, SEE_OTHER)
    }

    override fun toString(): String {
        return javaClass.simpleName + " [" + "wonderwall=" + wonderwall + "]"
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AAPApiExceptionHandler::class.java)
    }
}