package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import java.io.File
import java.io.IOException
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized

abstract class AbstractGraphQLAdapter(client: WebClient, cfg: AbstractRestConfig,
                                      val errorHandler: GraphQLErrorHandler = GraphQLDefaultErrorHandler()) :
    AbstractWebClientAdapter(client, cfg) {

    @Retryable(
            include = [WebClientException::class, IOException::class],
            exclude = [NotFound::class, Forbidden::class, Unauthorized::class],
            maxAttemptsExpression = "#{\${rest.retry.attempts:3}}",
            backoff = Backoff(delayExpression = "#{\${rest.retry.delay:100}}"))
    protected inline fun <reified T : Any> query(graphQLClient: GraphQLWebClient, query: String, ident: String) =
        runCatching {
            graphQLClient.post(query, ident.toIdent(), T::class.java).block()
        }.getOrElse {
            if (it is GraphQLErrorsException) {
                errorHandler.handle(it)
            }
            else {
                log.warn("Oppslag ${File(query).nameWithoutExtension.split("-")[0]} feilet med uventet feil", it)
                throw it
            }
        }
    protected inline fun <reified T : Any> queryBolk(graphQLClient: GraphQLWebClient, query: String, idents: List<String>) =
        runCatching {
            graphQLClient.flux(query, idents.toIdenter(), T::class.java).collectList().block()
        }.getOrElse {
            if (it is GraphQLErrorsException) {
                errorHandler.handle(it)
            }
            else {
                log.warn("Oppslag ${File(query).nameWithoutExtension.split("-")[0]} feilet med uventet feil", it)
                throw it
            }
        }

    companion object {
        private const val IDENT = "ident"
        private const val IDENTER = "identer"

        protected fun String.toIdent() = mapOf(IDENT to this)
        protected fun List<String>.toIdenter() = mapOf(IDENTER to this)
    }
}