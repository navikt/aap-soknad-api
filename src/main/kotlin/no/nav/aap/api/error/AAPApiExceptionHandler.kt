package no.nav.aap.api.error

import no.nav.aap.api.util.AuthContext
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@ControllerAdvice
class AAPApiExceptionHandler(val authContext: AuthContext) : ResponseEntityExceptionHandler() {
    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun handleMissingOrExpiredToken(e: java.lang.Exception, req: WebRequest): ResponseEntity<Any> {
         return handle(UNAUTHORIZED,e,req,HttpHeaders());
    }

    private  fun handle(status: HttpStatus, e: java.lang.Exception, req: WebRequest, vararg messages: Any): ResponseEntity<Any> {
        return handle(status, e, req, HttpHeaders(), *messages)
    }

    private  fun handle(status: HttpStatus, e: java.lang.Exception, req: WebRequest, headers: HttpHeaders, vararg messages: Any): ResponseEntity<Any> {
        return handle(status, e, req, headers, listOf(messages))
    }

    private  fun handle(status: HttpStatus, e: java.lang.Exception, req: WebRequest, headers: HttpHeaders, messages: List<Any>): ResponseEntity<Any> {
        return handleExceptionInternal(e, e.message, headers, status, req)
    }

    override fun toString() = "${javaClass.simpleName} [authContext=$authContext]"
}