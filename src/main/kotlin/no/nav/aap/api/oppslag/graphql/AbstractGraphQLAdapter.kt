package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import io.github.resilience4j.retry.annotation.Retry
import java.io.File
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.web.reactive.function.client.WebClient

abstract class AbstractGraphQLAdapter(client: WebClient, cfg: AbstractRestConfig,
                                      val errorHandler: GraphQLErrorHandler = GraphQLDefaultErrorHandler()) :
    AbstractWebClientAdapter(client, cfg) {

    @Retry(name = "graphql")
    protected inline fun <reified T> query(graphQLClient: GraphQLWebClient, query: String, ident: String) =
        runCatching {
            graphQLClient.post(query, ident.toIdent(), T::class.java).block()
        }.getOrElse {
            if (it is GraphQLErrorsException) {
                errorHandler.handle(it)
            }
            else {
                log.warn("Oppslag ${File(query).nameWithoutExtension.split("-")[1]} feilet med uventet exception ${it.javaClass.simpleName}", it)
                throw it
            }
        }

        protected inline fun <reified T> queryFlux(graphQLClient: GraphQLWebClient, query: String, idents: List<String>) =
        runCatching {
            graphQLClient.flux(query, idents.toIdenter(), T::class.java).collectList().block().also {
                log.trace("Flux returnerte $it")
            }
        }.getOrElse {
            if (it is GraphQLErrorsException) {
                errorHandler.handle(it)
            }
            else {
                log.warn("Oppslag ${File(query).nameWithoutExtension.split("-")[1]} feilet med uventet feil", it)
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