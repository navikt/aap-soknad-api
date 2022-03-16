package no.nav.aap.api.oppslag.organisasjon

import no.nav.aap.api.oppslag.organisasjon.OrganisasjonConfig.Companion.ORGANISASJON
import no.nav.aap.rest.AbstractWebClientAdapter
import org.apache.commons.lang3.StringUtils.capitalize
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@Component
class OrganisasjonWebClientAdapter(@Qualifier(ORGANISASJON)  val client: WebClient, private val cf: OrganisasjonConfig) : AbstractWebClientAdapter(client, cf) {

    @Cacheable(cacheNames = ["organisasjon"])
    fun orgNavn(orgnr: String?)  =
             webClient
                .get()
                .uri { b -> cf.getOrganisasjonURI(b, orgnr) }
                .accept(APPLICATION_JSON)
                .retrieve()
                .toEntity(String::class.java)
                .block()
                ?.body
                ?: orgnr
    override fun name() =  capitalize(ORGANISASJON.lowercase(Locale.getDefault()))
}