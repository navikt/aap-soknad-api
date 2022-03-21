package no.nav.aap.api.oppslag.organisasjon

import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.api.oppslag.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import no.nav.aap.rest.AbstractWebClientAdapter
import org.apache.commons.lang3.StringUtils.capitalize
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.Locale.getDefault

@Component
class OrganisasjonWebClientAdapter(@Qualifier(ORGANISASJON)  val client: WebClient, private val cf: OrganisasjonConfig) : AbstractWebClientAdapter(client, cf) {

    @Cacheable(cacheNames = ["organisasjon"])
    fun orgNavn(orgnr: OrgNummer)  =
             webClient
                .get()
                .uri { b -> cf.getOrganisasjonURI(b, orgnr) }
                .accept(APPLICATION_JSON)
                .retrieve()
                 .bodyToMono(String::class.java)
                 .doOnError { t: Throwable -> log.warn("EREG oppslag ${orgnr.orgnr} feilet", t) }
                 .onErrorReturn(orgnr.orgnr)
                 .defaultIfEmpty(orgnr.orgnr)
                 .blockOptional()
                 .orElse(orgnr.orgnr)


    override fun name() =  capitalize(ORGANISASJON.lowercase(getDefault()))
}