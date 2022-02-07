package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.PDL_SYSTEM
import no.nav.aap.util.Constants.PDL_USER
import no.nav.aap.util.LoggerUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient


@Component
class PDLWebClientAdapter(
        @Qualifier(PDL_USER) private val graphQLWebClient: GraphQLWebClient,
        @Qualifier(PDL_USER) webClient: WebClient,
        @Qualifier(PDL_SYSTEM) private val systemGraphQLWebClient: GraphQLWebClient,
        cfg: PDLConfig,
        private val authContext: AuthContext,
        private val errorHandler: PDLErrorHandler) : AbstractWebClientAdapter(webClient, cfg) {

    private val log = LoggerUtil.getLogger(javaClass)
    fun person(): PDLPerson? = authContext.getSubject()?.let { person(it) }

    private fun person(id: String): PDLPerson? {
        val p = oppslag({ graphQLWebClient.post(PERSON_QUERY, idFra(id), PDLWrappedPerson::class.java).block() }, "navn")
        log.info("Hentet person {}",p)
       // testBarn(id)
        return p?.active
    }

    private fun testBarn(id: String) {
        try {
            log.info("Henter barn for $id ")
            val b = barn(id)    
            log.info("Hentet barn $b for $id")

        } catch (e: Exception) {
            log.warn("Uffda", e)
        }
    }

    private fun barn(id: String) = oppslag({ systemGraphQLWebClient.post(BARN_QUERY, idFra(id), Map::class.java).block() }, "barn")

    private fun <T> oppslag(oppslag: () -> T, type: String): T {
        return try {
            oppslag.invoke()
        } catch (e: GraphQLErrorsException) {
            log.warn("PDL oppslag {} feilet", type, e)
            errorHandler.handleError(e)
        } catch (e: Exception) {
            log.warn("PDL oppslag {} feilet med uventet feil", type, e)
            throw e
        }
    }

    override fun ping() {
        webClient
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
            .toBodilessEntity()
            .block()
    }


    override fun toString() =
        "${javaClass.simpleName} [webClient=$webClient,graphQLWebClient=$graphQLWebClient,authContext=$authContext,errorHandler=$errorHandler, cfg=$cfg]"

    companion object {
        private const val IDENT = "ident"
        private const val PERSON_QUERY = "query-person.graphql"
        private const val BARN_QUERY = "query-barn.graphql"
        private fun idFra(id: String): Map<String, Any> = java.util.Map.of<String, Any>(IDENT, id)

    }

}