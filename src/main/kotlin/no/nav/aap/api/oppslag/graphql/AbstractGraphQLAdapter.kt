package no.nav.aap.api.oppslag.graphql

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import no.nav.aap.rest.AbstractRestConfig
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.AuthContext
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.web.reactive.function.client.WebClient

abstract class AbstractGraphQLAdapter(
    client: WebClient,
    cf: AbstractRestConfig,
    private val errorHandler: GraphQLErrorHandler
) :
    AbstractWebClientAdapter(client, cf) {

    protected fun <T> oppslag(oppslag: () -> T, type: String): T {
        return try {
            oppslag.invoke()
        } catch (e: GraphQLErrorsException) {
            errorHandler.handleError(e)
        } catch (e: Exception) {
            log.warn("Oppslag $type feilet med uventet feil", e)
            throw e
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

    protected fun fnr(ctx: AuthContext) = fnr(ctx.getFnr().fnr)
    protected fun fnr(fnr: String) = mapOf(IDENT to fnr)


    companion object {
        const val IDENT = "ident"
    }
}