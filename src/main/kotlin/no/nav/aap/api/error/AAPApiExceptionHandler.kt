package no.nav.aap.api.error

import no.nav.aap.api.util.AuthContext
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.zalando.problem.Problem
import org.zalando.problem.Status
import org.zalando.problem.ThrowableProblem
import org.zalando.problem.spring.web.advice.ProblemHandling


@ControllerAdvice
class AAPApiExceptionHandler(val authContext: AuthContext) :  ProblemHandling {
    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun handleMissingOrExpiredToken(e: java.lang.Exception, req: WebRequest): ThrowableProblem? {
        return Problem.valueOf(Status.UNAUTHORIZED);
    }

    override fun toString() = "${javaClass.simpleName} [authContext=$authContext]"
}