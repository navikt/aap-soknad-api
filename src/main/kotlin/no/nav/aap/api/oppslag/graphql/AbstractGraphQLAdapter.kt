package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.client.WebClient
import java.io.File

abstract class AbstractGraphQLAdapter(client: WebClient, cfg: AbstractRestConfig, val errorHandler: GraphQLErrorHandler = GraphQLDefaultErrorHandler()) :
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
    override fun ping() {
        webClient
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .toBodilessEntity()
            .block()
    }

    companion object {
        fun String.toIdent() = mapOf(IDENT to this)
        private const val IDENT = "ident"
    }
}