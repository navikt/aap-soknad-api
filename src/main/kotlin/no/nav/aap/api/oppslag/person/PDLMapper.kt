package no.nav.aap.api.oppslag.person

import java.time.LocalDate
import no.nav.aap.api.felles.Adresse
import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Navn
import no.nav.aap.api.felles.PostNummer
import no.nav.aap.api.oppslag.person.PDLAdresseBeskyttelse.FORTROLIG
import no.nav.aap.api.oppslag.person.PDLAdresseBeskyttelse.STRENGT_FORTROLIG
import no.nav.aap.api.oppslag.person.PDLAdresseBeskyttelse.STRENGT_FORTROLIG_UTLAND
import no.nav.aap.api.oppslag.person.PDLBolkBarn.PDLBarn
import no.nav.aap.api.oppslag.person.PDLSøker.PDLBostedadresse.PDLVegadresse
import no.nav.aap.api.oppslag.person.PDLSøker.PDLFødsel
import no.nav.aap.api.søknad.model.Søker
import no.nav.aap.api.søknad.model.Søker.Barn
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.boot.conditionals.EnvUtil.CONFIDENTIAL

object PDLMapper {
    private val log = getLogger(javaClass)

    fun pdlSøkerTilSøker(søker: PDLSøker?, fnr: Fødselsnummer, barn: Sequence<PDLBarn>) = søker?.let {
        with(it) {
            Søker(navnFra(navn), fnr,
                søker.beskyttet(),
                adresseFra(vegadresse),
                fødselsdatoFra(fødsel),
                pdlBarnTilBarn(barn)).also {
                log.trace(CONFIDENTIAL, "Søker er {}", it)
                }
        }
    }

    fun pdlBarnTilBarn(pdlBarn: Sequence<PDLBarn>) = pdlBarn
            .filterNot(::myndig)
            .filterNot(::beskyttet)
            .filterNot(::død)
            .map {
                Barn(navnFra(it.navn), fødselsdatoFra(it.fødselsdato), it.fnr)
            }.toList().also { log.trace("Mappet ${pdlBarn.toList()} til $it") }

    private fun navnFra(navn: Set<PDLNavn>) = navnFra(navn.first())


    fun beskyttedeBarn(fosterbarn: List<PDLBolkBarn>) = fosterbarn
        .map { it.barn }
        .filterNot(::død)
        .any(::beskyttet)

    private fun navnFra(navn: PDLNavn) =
        with(navn) {
            Navn(fornavn, mellomnavn, etternavn).also {
                log.trace(CONFIDENTIAL, "Navn er $it")
            }
        }

    private fun adresseFra(adresse: PDLVegadresse?) = adresse?.let {
        with(it) {
            Adresse(adressenavn, husbokstav, husnummer, PostNummer(postnummer))
        }
    }

    private fun fødselsdatoFra(fødsel: Set<PDLFødsel>?) = fødselsdatoFra(fødsel?.firstOrNull())

    private fun fødselsdatoFra(fødsel: PDLFødsel?) = fødsel?.fødselsdato

    private fun myndig(pdlBarn: PDLBarn) = fødselsdatoFra(pdlBarn.fødselsdato)?.isBefore(LocalDate.now().minusYears(18)) ?: true

    private fun beskyttet(pdlBarn: PDLBarn) = beskyttet(pdlBarn.adressebeskyttelse)

    private fun PDLSøker.beskyttet() = beskyttet(adressebeskyttelse)

    private fun død(pdlBarn: PDLBarn) = pdlBarn.dødsfall?.any() ?: false

    private fun beskyttet(gradering: Set<PDLGradering>?) = gradering?.any {
            it.gradering in listOf(FORTROLIG, STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG)
        } == true
}