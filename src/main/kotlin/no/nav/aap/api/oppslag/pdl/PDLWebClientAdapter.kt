package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.config.Constants.PDL_USER
import no.nav.aap.api.rest.AbstractWebClientAdapter
import no.nav.aap.api.util.AuthContext
import no.nav.aap.api.util.LoggerUtil.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
@Component
class PDLWebClientAdapter (@Qualifier(PDL_USER)  private val graphQLWebClient: GraphQLWebClient,
                           @Qualifier(PDL_USER) webClient: WebClient, cfg: PDLConfig,
                           private val authContext: AuthContext,
                           private val errorHandler: PDLErrorHandler) : AbstractWebClientAdapter(webClient, cfg) {

    private val log = getLogger(javaClass)
    internal fun navn(): PDLNavn? = authContext.getSubject()?.let { navn(it) }
    private fun navn(id: String): PDLNavn? = oppslag({ graphQLWebClient.post(NAVN_QUERY, idFra(id), PDLWrappedNavn::class.java).block() }, "navn")?.navn?.first()

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
        log.trace("Pinger {}", baseUri)
        webClient
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .onStatus({ obj: HttpStatus -> obj.isError }) { obj: ClientResponse -> obj.createException() }
            .toBodilessEntity()
            .block()
    }

    override fun toString() = "${javaClass.simpleName} [webClient=$webClient,graphQLWebClient=$graphQLWebClient,authContext=$authContext,errorHandler=$errorHandler, cfg=$cfg]"

    companion object {
        private const val IDENT = "ident"
        private const val NAVN_QUERY = "query-navn.graphql"
        private fun idFra(id: String): Map<String, Any> {
            return java.util.Map.of<String, Any>(IDENT, id)
        }
    }
}