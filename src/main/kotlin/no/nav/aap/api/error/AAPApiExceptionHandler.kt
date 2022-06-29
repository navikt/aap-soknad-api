package no.nav.aap.api.error

import com.google.cloud.storage.StorageException
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.mellomlagring.GCPBucketConfig.DokumentException
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo.UkjentContentTypeException
import no.nav.aap.util.LoggerUtil
import no.nav.aap.util.MDCUtil.callId
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
import org.zalando.problem.Status.UNSUPPORTED_MEDIA_TYPE
import org.zalando.problem.spring.web.advice.ProblemHandling

@ControllerAdvice
class AAPApiExceptionHandler : ProblemHandling {
    private val log = LoggerUtil.getLogger(javaClass)

    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun handleMissingOrExpiredToken(e: java.lang.Exception, req: NativeWebRequest): ResponseEntity<Problem> =
        create(e, Problem.builder()
            .withStatus(UNAUTHORIZED)
            .withDetail(e.message)
            .with("callid", callId()).build(), req).also { log.trace(UNAUTHORIZED.name, e) }

    @ExceptionHandler(IntegrationException::class)
    fun handleIntegrationException(e: IntegrationException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(e, Problem.builder()
            .withStatus(UNPROCESSABLE_ENTITY)
            .withDetail(e.message)
            .with("callid", callId()).build(), req).also { log.trace(UNPROCESSABLE_ENTITY.name, e) }

    @ExceptionHandler(DokumentException::class)
    fun handleDokumentException(e: DokumentException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(e, Problem.builder()
            .withStatus(UNPROCESSABLE_ENTITY)
            .withDetail(e.message)
            .with("callid", callId()).build(), req).also { log.trace(UNPROCESSABLE_ENTITY.name, e) }

    @ExceptionHandler(UkjentContentTypeException::class)
    fun handleUkjentContentTypeException(e: UkjentContentTypeException,
                                         req: NativeWebRequest): ResponseEntity<Problem> =
        create(e, Problem.builder()
            .withStatus(UNSUPPORTED_MEDIA_TYPE)
            .withDetail(e.message)
            .withTitle(e.message)
            .with("callid", callId()).build(), req).also { log.trace(UNSUPPORTED_MEDIA_TYPE.name, e) }

    @ExceptionHandler(StorageException::class)
    fun handleStorageException(e: StorageException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(e, Problem.builder()
            .withStatus(BAD_REQUEST)
            .withDetail(e.message)
            .with("callid", callId()).build(), req).also { log.trace(BAD_REQUEST.name, e) }

    @ExceptionHandler(HttpClientErrorException.NotFound::class)
    fun handleNotFound(e: HttpClientErrorException, req: NativeWebRequest): ResponseEntity<Problem> =
        create(NOT_FOUND, e, req).also { log.trace(NOT_FOUND.name, e) }
}