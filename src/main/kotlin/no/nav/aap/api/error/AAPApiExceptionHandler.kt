package no.nav.aap.api.error

import no.nav.aap.api.util.AuthContext
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.Problem
import org.zalando.problem.Status.*
import org.zalando.problem.spring.web.advice.ProblemHandling


@ControllerAdvice
class AAPApiExceptionHandler(val authContext: AuthContext) : ProblemHandling {

    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun handleMissingOrExpiredToken(e: java.lang.Exception, req: NativeWebRequest): ResponseEntity<Problem> =
        create(UNAUTHORIZED, e, req)

    @ExceptionHandler(IntegrationException::class)
    fun handleIntegrationException(e: java.lang.Exception, req: NativeWebRequest): ResponseEntity<Problem> =
        create(UNPROCESSABLE_ENTITY, e, req)

    @ExceptionHandler(HttpClientErrorException.NotFound::class)
    fun handleNotFound(e: java.lang.Exception, req: NativeWebRequest): ResponseEntity<Problem> =
        create(NOT_FOUND, e, req)

    override fun toString() = "${javaClass.simpleName} [authContext=$authContext]"
}