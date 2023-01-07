package no.nav.aap.api.error

import com.fasterxml.jackson.databind.DatabindException
import com.google.cloud.storage.StorageException
import no.nav.aap.api.felles.error.IntegrationException
import no.nav.aap.api.oppslag.graphql.GraphQLErrorExtensions.UnrecoverableGraphQL.BadGraphQL
import no.nav.aap.api.oppslag.graphql.GraphQLErrorExtensions.UnrecoverableGraphQL.NotFoundGraphQL
import no.nav.aap.api.oppslag.graphql.GraphQLErrorExtensions.UnrecoverableGraphQL.UnauthenticatedGraphQL
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.api.søknad.mellomlagring.dokument.GCPKryptertDokumentlager.ContentTypeDokumentSjekker.ContentTypeException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.MDCUtil.NAV_CALL_ID
import no.nav.aap.util.MDCUtil.callId
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest
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
    fun unauth(e: RuntimeException, req: NativeWebRequest) = createProblem(e, req, UNAUTHORIZED)

    @ExceptionHandler(UnauthenticatedGraphQL::class, UnauthenticatedGraphQL::class)
    fun unauthQL(e: RuntimeException, req: NativeWebRequest) = createProblem(e, req, UNAUTHORIZED)

    @ExceptionHandler(IntegrationException::class, StorageException::class)
    fun integration(e: RuntimeException, req: NativeWebRequest) = createProblem(e, req, SERVICE_UNAVAILABLE)

    @ExceptionHandler(ContentTypeException::class)
    fun ukjent(e: ContentTypeException, req: NativeWebRequest) = createProblem(e, req, UNSUPPORTED_MEDIA_TYPE)

    @ExceptionHandler(IllegalArgumentException::class, DatabindException::class)
    fun illegal(e: Exception, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    @ExceptionHandler(NotFound::class, NotFoundGraphQL::class)
    fun ikkeFunnet(e: Throwable, req: NativeWebRequest) = createProblem(e, req, NOT_FOUND)

    @ExceptionHandler(BadGraphQL::class, BadRequest::class)
    fun bad(e: Throwable, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    @ExceptionHandler(DokumentException::class)
    fun dokument(e: DokumentException, req: NativeWebRequest) = createProblem(e, req, UNPROCESSABLE_ENTITY, e.substatus)

    @ExceptionHandler(HttpMessageConversionException::class)
    fun messageConversion(e: HttpMessageConversionException, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)

    @ExceptionHandler(Exception::class)
    fun catchAll(e: Exception, req: NativeWebRequest) = createProblem(e, req, INTERNAL_SERVER_ERROR)

    override fun handleMessageNotReadableException(e: HttpMessageNotReadableException, req: NativeWebRequest) = createProblem(e, req, BAD_REQUEST)
     private fun createProblem(t: Throwable, req: NativeWebRequest, status: Status, substatus: Substatus? = null)  =
         create(t,toProblem(t, status, substatus), req)

    private fun toProblem(t: Throwable, status: Status, substatus: Substatus?) =
        with(builder()
            .withTitle(status.reasonPhrase)
            .withStatus(status)
            .withDetail(t.message)
            .with(NAV_CALL_ID, callId())) {
            substatus?.let {
                with(SUBSTATUS, it).build()
            } ?: build()
        }

    override fun log(t: Throwable, problem: Problem, req: NativeWebRequest, status: HttpStatus) =
        log.error("$req ${status.reasonPhrase}: ${ t.message}",t)

    companion object {
        private const val SUBSTATUS = "substatus"
    }
}