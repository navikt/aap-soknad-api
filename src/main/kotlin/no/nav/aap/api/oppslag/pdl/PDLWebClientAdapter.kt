package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLErrorsException
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.oppslag.pdl.PDLSøker.PDLBostedadresse.PDLVegadresse
import no.nav.aap.api.oppslag.pdl.PDLSøker.PDLForelderBarnRelasjon
import no.nav.aap.api.oppslag.pdl.PDLSøker.PDLFødsel
import no.nav.aap.api.søknad.model.Barn
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.PDL_SYSTEM
import no.nav.aap.util.Constants.PDL_USER
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient


@Component
class PDLWebClientAdapter(
        @Qualifier(PDL_USER) private val userWebClient: GraphQLWebClient,
        @Qualifier(PDL_USER) webClient: WebClient,
        @Qualifier(PDL_SYSTEM) private val systemWebClient: GraphQLWebClient,
        cfg: PDLConfig,
        private val ctx: AuthContext,
        private val errorHandler: PDLErrorHandler) : AbstractWebClientAdapter(webClient, cfg) {

    fun søker(medBarn: Boolean = false) = ctx.getSubject()?.let {fnr ->
        søkerOppslag(fnr)?.let {
            s -> søkerFra(s,medBarn)
        }
    }
    private fun søkerOppslag(fnr: String) = oppslag({
        userWebClient.post(PERSON_QUERY, idFra(fnr), PDLWrappedSøker::class.java).block()?.active
    }, "søker")

    private fun barnOppslag(fnr: String) =
        oppslag({
            systemWebClient.post(BARN_QUERY, idFra(fnr), PDLBarn::class.java).block() }, "barn")?.let {
            barn -> Barn(Fødselsnummer(fnr), navnFra(barn.navn.first()), barn.fødselsdato.firstOrNull()?.fødselsdato)
        }
    private fun fødselsdatoFra(fødsel: PDLFødsel?) = fødsel?.fødselsdato

    private fun søkerFra(søker: PDLSøker?,medBarn: Boolean) =  søker?.let { s ->
        Søker(
            navnFra(s.navn),
            adresseFra(s.vegadresse),
            fødselsdatoFra(s.fødsel),
            barnFra(s.forelderBarnRelasjon, medBarn))
    }

    private fun adresseFra(a: PDLVegadresse?) =  a?.let {
        Adresse(a.adressenavn,a.husbokstav,a.husnummer, PostNummer(a.postnummer))
    }

    private fun navnFra(n: PDLNavn) =  Navn(n.fornavn, n.mellomnavn, n.etternavn)

    private fun barnFra(r: List<PDLForelderBarnRelasjon>, medBarn: Boolean) = if (medBarn) r.map {
        barnOppslag(it.relatertPersonsIdent)
    } else emptyList()




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
            .toBodilessEntity()
            .block()
    }


    override fun toString() = "${javaClass.simpleName} [webClient=$webClient,graphQLWebClient=$userWebClient,authContext=$ctx,errorHandler=$errorHandler, cfg=$cfg]"


    companion object {
        private const val IDENT = "ident"
        private const val PERSON_QUERY = "query-person.graphql"
        private const val BARN_QUERY = "query-barn.graphql"
        private fun idFra(fnr: String): Map<String, Any> = java.util.Map.of<String, Any>(IDENT, fnr)

    }

}