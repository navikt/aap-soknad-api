package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import java.io.File
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.web.reactive.function.client.WebClient

abstract class AbstractGraphQLAdapter(client: WebClient, cfg: AbstractRestConfig,
                                      val errorHandler: GraphQLErrorHandler = GraphQLDefaultErrorHandler()) :
    AbstractWebClientAdapter(client, cfg) {

    protected inline fun <reified T : Any> query(graphQLClient: GraphQLWebClient, query: String, fnr: String) =
        runCatching {
            graphQLClient.post(query, fnr.toIdent(), T::class.java).block()
        }.getOrElse {
            if (it is GraphQLErrorsException) {
                errorHandler.handle(it)
            }
            else {
                log.warn("Oppslag ${File(query).nameWithoutExtension.split("-")[0]} feilet med uventet feil", it)
                throw it
            }
        }
    protected inline fun <reified T : Any> queryBolk(graphQLClient: GraphQLWebClient, query: String, fnrs: List<String>) =
        runCatching {
            graphQLClient.post(query, fnrs.toIdenter(), T::class.java).block()
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