package no.nav.aap.api.oppslag.krr

import no.nav.aap.api.oppslag.krr.KRRConfig.Companion.KRR
import no.nav.aap.rest.AbstractWebClientAdapter
import org.apache.commons.lang3.StringUtils.capitalize
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.*


@Component
class KRRWebClientAdapter(@Qualifier(KRR) client: WebClient, val cf: KRRConfig) :
    AbstractWebClientAdapter(client, cf) {

    fun kontaktInformasjon() =
         webClient.get()
            .uri(cf::kontaktUri)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(KontaktinformasjonDTO::class.java)
                .doOnSuccess {  log.trace("KOntaktinformasjon er $it")}
                .doOnError { t: Throwable -> log.warn("KRR oppslag målform feilet. Bruker default Målform", t) }
            .block()

    fun kontaktInformasjonM() =
        webClient.get()
            .uri(cf::kontaktUri)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(KontaktinformasjonDTO::class.java)
            .doOnSuccess {  log.trace("KOntaktinformasjon er $it")}
            .doOnError { t: Throwable -> log.warn("KRR oppslag målform feilet. Bruker default Målform", t) }


    override fun name(): String {
        return capitalize(KRR.lowercase(Locale.getDefault()))
    }

    override fun toString(): String {
        return javaClass.simpleName + " [cfg=" + cfg + "]"
    }
}