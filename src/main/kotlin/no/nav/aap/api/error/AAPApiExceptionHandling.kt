package no.nav.aap.api.error

import com.fasterxml.jackson.databind.DatabindException
import com.google.cloud.storage.StorageException
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.dokument.GCPKryptertDokumentlager.ContentTypeDokumentSjekker.ContentTypeException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.context.request.NativeWebRequest

@ControllerAdvice
class AAPApiExceptionHandling  {
    private val log = getLogger(javaClass)

    @ExceptionHandler(JwtTokenMissingException::class, JwtTokenUnauthorizedException::class)
    fun auth(e: RuntimeException, req: NativeWebRequest) = createProblem(e, req, UNAUTHORIZED)

    @ExceptionHandler(IntegrationException::class, StorageException::class)
    fun integration(e: RuntimeException, req: NativeWebRequest) = createProblem(e, req, SERVICE_UNAVAILABLE)

    @ExceptionHandler(ContentTypeException::class)
    fun ukjent(e: ContentTypeException, req: NativeWebRequest) = createProblem(e, req, UNSUPPORTED_MEDIA_TYPE)

    @ExceptionHandler(IllegalArgumentException::class, DatabindException::class)
    fun illegal(e: Exception, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    @ExceptionHandler(NotFound::class)
    fun ikkeFunnet(e: NotFound, req: NativeWebRequest) = createProblem(e, req, NOT_FOUND)

    @ExceptionHandler(DokumentException::class)
    fun dokument(e: DokumentException, req: NativeWebRequest) = createProblem(e, req, UNPROCESSABLE_ENTITY, e.substatus)

    @ExceptionHandler(HttpMessageConversionException::class)
    fun messageConversion(e: HttpMessageConversionException, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    @ExceptionHandler(Exception::class)
    fun catchAll(e: Exception, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

     private fun createProblem(t: Throwable, req: NativeWebRequest, status: HttpStatus, substatus: Substatus? = null)  =
         toProblem(t, status, substatus,req)

    private fun toProblem(t: Throwable, status: HttpStatus, substatus: Substatus?,req: NativeWebRequest) =
        ProblemDetail.forStatus(status).apply {
            title = status.reasonPhrase
            detail = t.message
            setProperty(NAV_CALL_ID, callId())
            substatus?.let {
                setProperty(SUBSTATUS, it)
            }
        }.also { log(t,it,req,status) }

     private fun log(t: Throwable, problem: ProblemDetail, req: NativeWebRequest, status: HttpStatus) =
        log.error("$req $problem ${status.reasonPhrase}: ${ t.message}",t)

    companion object {
        private const val SUBSTATUS = "substatus"
    }
}