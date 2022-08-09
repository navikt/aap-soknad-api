package no.nav.aap.api.error

import com.google.cloud.storage.StorageException
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.DokumentException.Substatus
import no.nav.aap.api.søknad.mellomlagring.dokument.GCPKryptertDokumentlager.ContentTypeDokumentSjekker.ContentTypeException
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
class AAPApiExceptionHandler : ProblemHandling {
    private val log = getLogger(javaClass)

    @ExceptionHandler(JwtTokenUnauthorizedException::class, JwtTokenMissingException::class)
    fun tokenProblem(e: RuntimeException, req: NativeWebRequest) =
        problem(e, UNAUTHORIZED, req)

    @ExceptionHandler(IntegrationException::class)
    fun integrasjon(e: IntegrationException, req: NativeWebRequest) =
        problem(e, UNPROCESSABLE_ENTITY, req)

    @ExceptionHandler(DokumentException::class)
    fun dokument(e: DokumentException, req: NativeWebRequest) =
        problem(e, UNPROCESSABLE_ENTITY, req)

    @ExceptionHandler(ContentTypeException::class)
    fun ukjent(e: ContentTypeException, req: NativeWebRequest) =
        problem(e, UNSUPPORTED_MEDIA_TYPE, req)

    @ExceptionHandler(StorageException::class)
    fun bøtte(e: StorageException, req: NativeWebRequest) = problem(e, BAD_REQUEST, req)

    @ExceptionHandler(NotFound::class)
    fun ikkeFunnet(e: NotFound, req: NativeWebRequest) = problem(e, NOT_FOUND, req)

    fun problem(e: DokumentException, status: Status, req: NativeWebRequest) =
        create(e, problem(e, status, e.substatus), req)

    fun problem(e: RuntimeException, status: Status, req: NativeWebRequest) = create(e, problem(e, status, null), req)

    private fun problem(e: RuntimeException, status: Status, substatus: Substatus? = null) =
        with(builder().withStatus(status).withDetail(e.message).with(NAV_CALL_ID, callId())) {
            substatus?.let { with("substatus", it).build() } ?: build()
        }.also { log.warn("Problem $it", e) }
}