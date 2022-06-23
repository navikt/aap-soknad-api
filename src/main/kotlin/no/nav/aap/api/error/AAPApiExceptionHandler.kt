package no.nav.aap.api.error

import com.google.cloud.storage.StorageException
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.virus.AttachmentException
import no.nav.aap.util.LoggerUtil
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.Problem
import org.zalando.problem.Status.BAD_REQUEST
import org.zalando.problem.Status.NOT_FOUND
import org.zalando.problem.Status.UNAUTHORIZED
import org.zalando.problem.Status.UNPROCESSABLE_ENTITY
import org.zalando.problem.spring.web.advice.ProblemHandling

@ControllerAdvice
class AAPApiExceptionHandler : ProblemHandling {
    private val log = LoggerUtil.getLogger(javaClass)

    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun handleMissingOrExpiredToken(e: java.lang.Exception, req: NativeWebRequest): ResponseEntity<Problem> =
        create(UNAUTHORIZED, e, req).also { log.trace(UNAUTHORIZED.name, e) }

    @ExceptionHandler(IntegrationException::class)
    fun handleIntegrationException(e: IntegrationException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(UNPROCESSABLE_ENTITY, e, req).also { log.trace(UNPROCESSABLE_ENTITY.name, e) }

    @ExceptionHandler(AttachmentException::class)
    fun handleVirus(e: AttachmentException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(UNPROCESSABLE_ENTITY, e, req).also { log.trace(UNPROCESSABLE_ENTITY.name, e) }

    @ExceptionHandler(StorageException::class)
    fun handleStorageException(e: StorageException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(BAD_REQUEST, e, req).also { log.trace(BAD_REQUEST.name, e) }

    @ExceptionHandler(HttpClientErrorException.NotFound::class)
    fun handleNotFound(e: HttpClientErrorException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(NOT_FOUND, e, req).also { log.trace(NOT_FOUND.name, e) }
}