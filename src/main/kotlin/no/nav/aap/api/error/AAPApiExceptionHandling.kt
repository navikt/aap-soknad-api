package no.nav.aap.api.error

import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
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
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.Problem
import org.zalando.problem.Problem.builder
import org.zalando.problem.Status
import org.zalando.problem.Status.BAD_REQUEST
import org.zalando.problem.Status.INTERNAL_SERVER_ERROR
import org.zalando.problem.Status.NOT_FOUND
import org.zalando.problem.Status.SERVICE_UNAVAILABLE
import org.zalando.problem.Status.UNAUTHORIZED
import org.zalando.problem.Status.UNPROCESSABLE_ENTITY
import org.zalando.problem.Status.UNSUPPORTED_MEDIA_TYPE
import org.zalando.problem.spring.web.advice.ProblemHandling

@ControllerAdvice
class AAPApiExceptionHandling : ProblemHandling {
    private val log = getLogger(javaClass)

    @ExceptionHandler(JwtTokenMissingException::class, JwtTokenUnauthorizedException::class)
    fun auth(e: RuntimeException, req: NativeWebRequest) = createProblem(UNAUTHORIZED, e, req)

    @ExceptionHandler(IntegrationException::class, StorageException::class)
    fun inegration(e: RuntimeException, req: NativeWebRequest) = createProblem(SERVICE_UNAVAILABLE, e, req)

    @ExceptionHandler(ContentTypeException::class)
    fun ukjent(e: ContentTypeException, req: NativeWebRequest) = createProblem(UNSUPPORTED_MEDIA_TYPE, e, req)

    @ExceptionHandler(IllegalArgumentException::class, DatabindException::class)
    fun illegal(e: Exception, req: NativeWebRequest) = createProblem(BAD_REQUEST, e, req)

    @ExceptionHandler(NotFound::class)
    fun ikkeFunnet(e: NotFound, req: NativeWebRequest) = createProblem(NOT_FOUND, e, req)

    @ExceptionHandler(DokumentException::class)
    fun dokument(e: DokumentException, req: NativeWebRequest) = createProblem(UNPROCESSABLE_ENTITY,e, req, e.substatus)

    @ExceptionHandler(Exception::class)
    fun catchAll(e: Exception, req: NativeWebRequest) = createProblem(INTERNAL_SERVER_ERROR, e, req)

    @ExceptionHandler(InvalidFormatException::class)
    fun invalidFormat(e: InvalidFormatException, req: NativeWebRequest) = createProblem(BAD_REQUEST, e, req)
     fun createProblem(status: Status, t: Throwable, request: NativeWebRequest, substatus: Substatus? = null)  =
         create(t,toProblem(t,status,substatus), request)

    override fun log(throwable: Throwable, problem: Problem, request: NativeWebRequest, status: HttpStatus) {
        if (status.is4xxClientError) {
            log.error("XX {}: {}", status.reasonPhrase, throwable.message,throwable)
        }
        else if (status.is5xxServerError) {
            log.error("XX " + status.reasonPhrase, throwable)
        }
    }
    private fun toProblem(t: Throwable, status: Status, substatus: Substatus? = null) =
        with(builder().withStatus(status).withDetail(t.message).with(NAV_CALL_ID, callId())) {
            substatus?.let {
                with("substatus", it).build()
            } ?: build()
        }.also {
            log.trace("Returnerer $status", it)
        }
}