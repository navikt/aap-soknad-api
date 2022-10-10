package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import java.time.LocalDate
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.oppslag.graphql.AbstractGraphQLAdapter
import no.nav.aap.api.oppslag.pdl.PDLBarn.PDLAdresseBeskyttelse.FORTROLIG
import no.nav.aap.api.oppslag.pdl.PDLBarn.PDLAdresseBeskyttelse.STRENGT_FORTROLIG
import no.nav.aap.api.oppslag.pdl.PDLBarn.PDLAdresseBeskyttelse.STRENGT_FORTROLIG_UTLAND
import no.nav.aap.api.oppslag.pdl.PDLBarn.PDLBarnBolk
import no.nav.aap.api.oppslag.pdl.PDLSøker.PDLBostedadresse.PDLVegadresse
import no.nav.aap.api.oppslag.pdl.PDLSøker.PDLForelderBarnRelasjon
import no.nav.aap.api.oppslag.pdl.PDLSøker.PDLFødsel
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.Søker.Barn
import no.nav.aap.util.AuthContext
import no.nav.aap.util.Constants.PDL_SYSTEM
import no.nav.aap.util.Constants.PDL_USER
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
data class WebClients(
        @Qualifier(PDL_USER) val client: WebClient,
        @Qualifier(PDL_USER) val user: GraphQLWebClient,
        @Qualifier(PDL_SYSTEM) val system: GraphQLWebClient)

@Component
class PDLWebClientAdapter(private val clients: WebClients, cfg: PDLConfig, private val ctx: AuthContext) :
    AbstractGraphQLAdapter(clients.client, cfg) {


    override fun ping() :Map<String,String>{
        webClient
            .options()
            .uri(baseUri)
            .accept(APPLICATION_JSON, TEXT_PLAIN)
            .retrieve()
            .toBodilessEntity()
            .block()
        return emptyMap()
    }

    fun søker(medBarn: Boolean = false) =
        with(ctx.getFnr()) {
            query<PDLWrappedSøker>(clients.user, PERSON_QUERY, fnr)?.active?.let {
                søkerFra(it, this, medBarn)
            } ?: throw JwtTokenMissingException()
        }

    private fun fødselsdatoFra(fødsel: Set<PDLFødsel>?) = fødselsdatoFra(fødsel?.firstOrNull())

    private fun fødselsdatoFra(fødsel: PDLFødsel?) = fødsel?.fødselsdato

    private fun søkerFra(søker: PDLSøker?, fnr: Fødselsnummer, medBarn: Boolean) = søker?.let {
        with(it) {
            Søker(navnFra(navn), fnr,
                    adresseFra(vegadresse),
                    fødselsdatoFra(fødsel),
                    barnFra(forelderBarnRelasjon, medBarn))
                .also { log.trace(CONFIDENTIAL, "Søker er $it")
                    try {
                        log.trace("Slår opp barn")
                        log.trace("BARN BOLK ${barnBolkFra(forelderBarnRelasjon)}")
                    }
                    catch (e: Exception) {
                        log.trace("OOPS",e)
                    }
                }
        }
    }

    private fun barnFra(r: List<PDLForelderBarnRelasjon>, medBarn: Boolean) =
        if (medBarn) {
            r.asSequence().map {
                query<PDLBarn>(clients.system, BARN_QUERY, it.relatertPersonsIdent)
            }.filterNotNull()
                .filterNot(::myndig)
                .filterNot(::beskyttet)
                .filterNot(::død)
                .map { Barn(navnFra(it.navn), fødselsdatoFra(it.fødselsdato)) }.toList()
        }
        else emptyList()

    private fun barnBolkFra(r: List<PDLForelderBarnRelasjon>) =
        queryBolk<List<PDLBarnBolk>>(clients.system, BARN_BOLK_QUERY, r.map { it.relatertPersonsIdent })




    private fun adresseFra(adresse: PDLVegadresse?) = adresse?.let {
        with(it) {
            Adresse(adressenavn, husbokstav, husnummer, PostNummer(postnummer))
        }
    }

    private fun navnFra(navn: Set<PDLNavn>) = navnFra(navn.first())

    private fun navnFra(navn: PDLNavn) =
        with(navn) {
            Navn(fornavn, mellomnavn, etternavn)
                .also { log.trace(CONFIDENTIAL, "Navn er $it") }
        }

    private fun myndig(pdlBarn: PDLBarn) =
        fødselsdatoFra(pdlBarn.fødselsdato)?.isBefore(LocalDate.now().minusYears(18)) ?: true

    private fun beskyttet(pdlBarn: PDLBarn) = pdlBarn.adressebeskyttelse?.any {
        it in listOf(FORTROLIG, STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG)
    } == true

    private fun død(pdlBarn: PDLBarn) = pdlBarn.dødsfall?.any() ?: false

    override fun toString() =
        "${javaClass.simpleName} [webClient=$webClient,webClients=$clients,authContext=$ctx, cfg=$cfg]"

    companion object {
        private const val BARN_BOLK_QUERY = "query-barnbolk.graphql"
        private const val PERSON_QUERY = "query-person.graphql"
        private const val BARN_QUERY = "query-barn.graphql"
    }
}