package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.oppslag.graphql.AbstractGraphQLAdapter
import no.nav.aap.api.oppslag.pdl.PDLBarn.PDLAdresseBeskyttelse.FORTROLIG
import no.nav.aap.api.oppslag.pdl.PDLBarn.PDLAdresseBeskyttelse.STRENGT_FORTROLIG
import no.nav.aap.api.oppslag.pdl.PDLBarn.PDLAdresseBeskyttelse.STRENGT_FORTROLIG_UTLAND
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
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

@Component
data class WebClients(
        @Qualifier(PDL_USER) val client: WebClient,
        @Qualifier(PDL_USER)  val user: GraphQLWebClient,
        @Qualifier(PDL_SYSTEM) val system: GraphQLWebClient)
@Component
class PDLWebClientAdapter(private val clients: WebClients, cfg: PDLConfig, private val ctx: AuthContext) : AbstractGraphQLAdapter(clients.client, cfg) {

    fun søker(medBarn: Boolean = false) =
        with(ctx.getFnr()) {
            query<PDLWrappedSøker>(clients.user, PERSON_QUERY, this.fnr)?.active?.let {
                søkerFra(it,this, medBarn)
            } ?: throw JwtTokenMissingException()
        }
    private fun fødselsdatoFra(fødsel: Set<PDLFødsel>?) = fødselsdatoFra(fødsel?.firstOrNull())

    private fun fødselsdatoFra(fødsel: PDLFødsel?) = fødsel?.fødselsdato

    private fun søkerFra(søker: PDLSøker?, fnr: Fødselsnummer, medBarn: Boolean) = søker?.let { s ->
        Søker(navnFra(s.navn),
                fnr,
                adresseFra(s.vegadresse),
                fødselsdatoFra(s.fødsel),
                barnFra(s.forelderBarnRelasjon, medBarn))
            .also { log.trace(CONFIDENTIAL, "Søker er $it") }
    }

    private fun barnFra(r: List<PDLForelderBarnRelasjon>, medBarn: Boolean) =
        if (medBarn) {
            r.asSequence().map {
                query<PDLBarn>(clients.system, BARN_QUERY, it.relatertPersonsIdent)
            }.filterNotNull()
                .filterNot(::myndig)
                .filterNot(::beskyttet)
                .map { Barn(navnFra(it.navn), fødselsdatoFra(it.fødselsdato)) }.toList()
        }
        else emptyList()

    private fun adresseFra(a: PDLVegadresse?) = a?.let {
        Adresse(a.adressenavn, a.husbokstav, a.husnummer, PostNummer(a.postnummer))
    }

    private fun navnFra(n: Set<PDLNavn>) = navnFra(n.first())

    private fun navnFra(n: PDLNavn) = Navn(n.fornavn, n.mellomnavn, n.etternavn)
        .also { log.trace(CONFIDENTIAL, "Navn er $it") }

    private fun myndig(pdlBarn: PDLBarn) = fødselsdatoFra(pdlBarn.fødselsdato)?.isBefore(LocalDate.now().minusYears(18)) ?: true
    private fun beskyttet(pdlBarn: PDLBarn) = pdlBarn.adressebeskyttelse?.any { it !in listOf(FORTROLIG, STRENGT_FORTROLIG_UTLAND,STRENGT_FORTROLIG) } == true
    override fun toString() = "${javaClass.simpleName} [webClient=$webClient,webClients=$clients,authContext=$ctx, cfg=$cfg]"

    companion object {
        private const val PERSON_QUERY = "query-person.graphql"
        private const val BARN_QUERY = "query-barn.graphql"
    }
}