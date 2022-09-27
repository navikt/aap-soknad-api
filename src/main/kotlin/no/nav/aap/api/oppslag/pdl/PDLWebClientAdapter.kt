package no.nav.aap.api.oppslag.pdl

import graphql.kickstart.spring.webclient.boot.GraphQLWebClient
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.oppslag.graphql.AbstractGraphQLAdapter
import no.nav.aap.api.oppslag.pdl.PDLBarn.PDLAdresseBeskyttelse
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
class PDLWebClientAdapter(
        @Qualifier(PDL_USER) private val userWebClient: GraphQLWebClient,
        @Qualifier(PDL_USER) client: WebClient,
        @Qualifier(PDL_SYSTEM) private val systemWebClient: GraphQLWebClient,
        cfg: PDLConfig,
        private val ctx: AuthContext) :
    AbstractGraphQLAdapter(client, cfg) {

    fun søker(medBarn: Boolean = false) =
        with(ctx.getFnr()) {
            query<PDLWrappedSøker>(userWebClient, PERSON_QUERY, this.fnr)?.active?.let {
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

    private fun adresseFra(a: PDLVegadresse?) = a?.let {
        Adresse(a.adressenavn, a.husbokstav, a.husnummer, PostNummer(a.postnummer))
            .also { log.trace(CONFIDENTIAL, "Adresse er $it") }
    }

    private fun navnFra(n: Set<PDLNavn>) = navnFra(n.first())

    private fun navnFra(n: PDLNavn) = Navn(n.fornavn, n.mellomnavn, n.etternavn)
        .also { log.trace(CONFIDENTIAL, "Navn er $it") }

    private fun barnFra(r: List<PDLForelderBarnRelasjon>, medBarn: Boolean): List<Barn?> =
        if (medBarn) r.map { it ->
            query<PDLBarn>(systemWebClient, BARN_QUERY, it.relatertPersonsIdent)

                .let { barn ->
                    if (barn.adressebeskyttelse?.any { it in listOf(FORTROLIG,STRENGT_FORTROLIG,STRENGT_FORTROLIG_UTLAND)} == true) {
                        null  // kode 6 og 7
                    }
                    val b = Barn(navnFra(barn.navn), fødselsdatoFra(barn.fødselsdato))
                    b.fødseldato?.let {
                        if (it.isBefore(LocalDate.now().minusYears(18))) {
                            null// myndig
                        }
                        else b
                    } ?: b
                }
                .also { b -> log.trace(CONFIDENTIAL, "Barn er $b") }
        }
        else emptyList()

    override fun toString() =
        "${javaClass.simpleName} [webClient=$webClient,graphQLWebClient=$userWebClient,authContext=$ctx, cfg=$cfg]"

    companion object {
        private const val PERSON_QUERY = "query-person.graphql"
        private const val BARN_QUERY = "query-barn.graphql"
    }
}