package no.nav.aap.api.oppslag.arbeid

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.aap.api.felles.OrgNummer
import no.nav.aap.rest.AbstractRetryingWebClientAdapter
import no.nav.aap.util.Constants.ORGANISASJON
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class OrganisasjonWebClientAdapter(@Qualifier(ORGANISASJON) val client: WebClient,
                                   private val cf: OrganisasjonConfig) : AbstractRetryingWebClientAdapter(client, cf) {

    fun orgNavn(orgnr: OrgNummer) =
        if (cf.isEnabled) {
            webClient
                .get()
                .uri { b ->
                    cf.organisasjonURI(b, orgnr)
                }
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus({ it.isError }) { Mono.empty() }
                .bodyToMono(OrganisasjonDTO::class.java)
                .doOnError { t: Throwable ->
                    log.warn("Organisasjon oppslag feilet", t)
                }
                .doOnSuccess {
                    log.trace("Organisasjon oppslag OK")
                }
                .mapNotNull(OrganisasjonDTO::fulltNavn)
                .defaultIfEmpty(orgnr.orgnr)
                .block() ?: orgnr.orgnr
                .also {
                    log.trace("Organisasjon oppslag response $it")
                }
        }
        else {
            orgnr.orgnr
        }

}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OrganisasjonDTO(val navn: OrganisasjonNavnDTO) {
    val fulltNavn = with(navn) {
        listOfNotNull(navnelinje1, navnelinje2, navnelinje3, navnelinje4, navnelinje5).joinToString(" ")
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    internal data class OrganisasjonNavnDTO(val navnelinje1: String?,
                                            val navnelinje2: String?,
                                            val navnelinje3: String?,
                                            val navnelinje4: String?,
                                            val navnelinje5: String?)
}