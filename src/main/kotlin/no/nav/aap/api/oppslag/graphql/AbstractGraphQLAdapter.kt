package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.client.WebClient
import java.io.File

abstract class AbstractGraphQLAdapter(client: WebClient, cf: AbstractRestConfig, val  errorHandler: GraphQLErrorHandler = GraphQLExceptionThrowingErrorHandler()) : AbstractWebClientAdapter(client, cf) {

    protected inline fun  <reified T: Any> query(client: GraphQLWebClient, query: String, fnr: String) =
        try {
            client.post(query, fnr.toIdent(), T::class.java).block()
        } catch (e: GraphQLErrorsException) {
            errorHandler.handle(e)
        } catch (e: Exception) {
            log.warn("Oppslag ${File(query).nameWithoutExtension.split("-")[0]} feilet med uventet feil", e)
            throw e
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
        fun String.toIdent() =  mapOf(IDENT to this)
        private const val IDENT = "ident"
    }
}