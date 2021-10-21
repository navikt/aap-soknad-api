package no.nav.aap.api.error

import no.nav.aap.api.util.AuthContext
import no.nav.foreldrepenger.boot.conditionals.EnvUtil.isDevOrLocal
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.zalando.problem.Problem
import org.zalando.problem.Status
import org.zalando.problem.spring.webflux.advice.ProblemHandling


@ControllerAdvice
class AAPApiExceptionHandler(val authContext: AuthContext, private val env: Environment) : ProblemHandling {

    private val LOG = LoggerFactory.getLogger(AAPApiExceptionHandler::class.java)

    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun handleMissingOrExpiredToken(e: java.lang.Exception, req: WebRequest) {
         throw Problem.valueOf(Status.UNAUTHORIZED);
    }

    override fun isCausalChainsEnabled(): Boolean {
        return isDevOrLocal(env);
    }


    override fun toString() = "${javaClass.simpleName} [authContext=$authContext]"
}