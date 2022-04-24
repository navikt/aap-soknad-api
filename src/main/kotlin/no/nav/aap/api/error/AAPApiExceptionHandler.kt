package no.nav.aap.api.error

import com.google.cloud.storage.StorageException
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.mellomlagring.virus.AttachmentVirusException
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

    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun handleMissingOrExpiredToken(e: java.lang.Exception, req: NativeWebRequest): ResponseEntity<Problem> =
        create(UNAUTHORIZED, e, req)

    @ExceptionHandler(IntegrationException::class)
    fun handleIntegrationException(e: IntegrationException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(UNPROCESSABLE_ENTITY, e, req)

    @ExceptionHandler(AttachmentVirusException::class)
    fun handleVirus(e: AttachmentVirusException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(UNPROCESSABLE_ENTITY, e, req)

    @ExceptionHandler(StorageException::class)
    fun handleStorageException(e: StorageException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(BAD_REQUEST, e, req)

    @ExceptionHandler(HttpClientErrorException.NotFound::class)
    fun handleNotFound(e: HttpClientErrorException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(NOT_FOUND, e, req)
}