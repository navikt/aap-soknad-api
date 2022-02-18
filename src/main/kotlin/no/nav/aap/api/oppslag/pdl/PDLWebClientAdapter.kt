package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.oppslag.pdl.PDLSøker.PDLForelderBarnRelasjon
import no.nav.aap.api.søknad.model.Barn
import no.nav.aap.api.søknad.model.Søker
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
        @Qualifier(PDL_USER) private val userWebClient: GraphQLWebClient,
        @Qualifier(PDL_USER) webClient: WebClient,
        @Qualifier(PDL_SYSTEM) private val systemWebClient: GraphQLWebClient,
        cfg: PDLConfig,
        private val authContext: AuthContext,
        private val errorHandler: PDLErrorHandler) : AbstractWebClientAdapter(webClient, cfg) {

    private val log = LoggerUtil.getLogger(javaClass)
    fun søker(medBarn: Boolean = false) = authContext.getSubject()?.let {
        søkerFra(it,medBarn)
    }

    private fun søkerFra(id: String, medBarn: Boolean): Søker? {
        val søker = oppslag({ userWebClient.post(PERSON_QUERY, idFra(id), PDLWrappedSøker::class.java).block() }, "søker")?.active
        return søker?.let {
            Søker(Navn(it.navn.fornavn, it.navn.mellomnavn, it.navn.etternavn), it.fødsel?.fødselsdato, barnFra(it.forelderBarnRelasjon, medBarn))
        }
    }

    private fun barnFra(relasjoner: List<PDLForelderBarnRelasjon>, medBarn: Boolean): List<Barn?> {
        if (medBarn) {
            return relasjoner.map { barn(it.relatertPersonsIdent) }
        }
        return emptyList()
    }

    fun barn(id: String): Barn? {
        val b =  oppslag({ systemWebClient.post(BARN_QUERY, idFra(id), PDLBarn::class.java).block() }, "barn")
        return b?.let { navn(it)?.let { it1 -> Barn(it1, it.fødselsdato.firstOrNull()?.fødselsdato) } }
    }

    private fun navn(b: PDLBarn?): Navn? {
        return b?.navn?.firstOrNull()?.let { Navn(it.fornavn, it.mellomnavn, it.etternavn) }
    }

    private fun <T> oppslag(oppslag: () -> T, type: String): T {
        return try {
            oppslag.invoke()
        } catch (e: GraphQLErrorsException) {
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


    override fun toString() = "${javaClass.simpleName} [webClient=$webClient,graphQLWebClient=$userWebClient,authContext=$authContext,errorHandler=$errorHandler, cfg=$cfg]"


    companion object {
        private const val IDENT = "ident"
        private const val PERSON_QUERY = "query-person.graphql"
        private const val BARN_QUERY = "query-barn.graphql"
        private fun idFra(id: String): Map<String, Any> = java.util.Map.of<String, Any>(IDENT, id)

    }

}