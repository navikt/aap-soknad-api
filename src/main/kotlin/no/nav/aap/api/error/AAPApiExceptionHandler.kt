package no.nav.aap.api.error

import com.google.cloud.storage.StorageException
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager.Companion.FNR
import no.nav.aap.api.søknad.mellomlagring.dokument.GCPKMSKeyKryptertDokumentlager.ContentTypeDokumentSjekker.ContentTypeException
import no.nav.aap.util.AuthContext
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.Problem.builder
import org.zalando.problem.Status
import org.zalando.problem.Status.BAD_REQUEST
import org.zalando.problem.Status.NOT_FOUND
import org.zalando.problem.Status.UNAUTHORIZED
import org.zalando.problem.Status.UNPROCESSABLE_ENTITY
import org.zalando.problem.Status.UNSUPPORTED_MEDIA_TYPE
import org.zalando.problem.spring.web.advice.ProblemHandling

@ControllerAdvice
class AAPApiExceptionHandler(private val ctx: AuthContext) : ProblemHandling {
    private val log = getLogger(javaClass)

    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun handleMissingOrExpiredToken(e: RuntimeException, req: NativeWebRequest) = handle(e, UNAUTHORIZED, req)

    @ExceptionHandler(IntegrationException::class)
    fun handleIntegrationException(e: IntegrationException, req: NativeWebRequest) =
        handle(e, UNPROCESSABLE_ENTITY, req)

    @ExceptionHandler(DokumentException::class)
    fun handleDokumentException(e: DokumentException, req: NativeWebRequest) = handle(e, UNPROCESSABLE_ENTITY, req)

    @ExceptionHandler(ContentTypeException::class)
    fun handleUkjentContentTypeException(e: ContentTypeException, req: NativeWebRequest) =
        handle(e, UNSUPPORTED_MEDIA_TYPE, req)

    @ExceptionHandler(StorageException::class)
    fun handleStorageException(e: StorageException, req: NativeWebRequest) = handle(e, BAD_REQUEST, req)

    @ExceptionHandler(NotFound::class)
    fun handleNotFound(e: NotFound, req: NativeWebRequest) = handle(e, NOT_FOUND, req)

    fun handle(e: RuntimeException, status: Status, req: NativeWebRequest) = create(e, problemFra(e, status), req)
    private fun problemFra(e: RuntimeException, status: Status) =
        builder()
            .withStatus(status)
            .withDetail(e.message)
            .with(FNR, ctx.getSubject())
            .with(NAV_CALL_ID, callId()).build().also {
                log.trace("status er $it", e)
            }
}