package no.nav.aap.api.søknad.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.søknad.pdl.PDLClientConfig.Companion.PDL_USER
import no.nav.aap.api.søknad.rest.AbstractWebClientAdapter
import no.nav.aap.api.søknad.tokenx.AuthContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient

@Component
class PDLWebClientAdapter internal constructor(
    @Qualifier(PDL_USER)  private val graphQLWebClient: GraphQLWebClient,
    @Qualifier(PDL_USER) webClient: WebClient, cfg: PDLConfig,
    private val authContext: AuthContext,
    private val errorHandler: PDLErrorHandler
) : AbstractWebClientAdapter(webClient, cfg) {
    internal fun navn(): PDLNavn? {
        return authContext.subject?.let { navn(it) }
    }

    private fun navn(id: String): PDLNavn? {
        val navneQuery = { graphQLWebClient.post(NAVN_QUERY, idFra(id), PDLWrappedNavn::class.java).block() }
        val n = oppslag(navneQuery, "navn");
        return n?.navn?.first()
    }

    private fun <T> oppslag(oppslag: () -> T, type: String): T {
        return try {
            LOG.info("PDL oppslag {}", type)
            val res = oppslag.invoke()
            LOG.trace("PDL oppslag {} respons={}", type, res)
            LOG.info("PDL oppslag {} OK", type)
            res
        } catch (e: GraphQLErrorsException) {
            LOG.warn("PDL oppslag {} feilet", type, e)
            errorHandler.handleError(e)
        } catch (e: Exception) {
            LOG.warn("PDL oppslag {} feilet med uventet feil", type, e)
            throw e
        }
    }

    override fun ping() {
        LOG.trace("Pinger {}", baseUri)
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
        private val LOG = LoggerFactory.getLogger(PDLWebClientAdapter::class.java)
        private const val IDENT = "ident"
        private const val NAVN_QUERY = "query-navn.graphql"
        private fun idFra(id: String): Map<String, Any> {
            return java.util.Map.of<String, Any>(IDENT, id)
        }
    }
}
