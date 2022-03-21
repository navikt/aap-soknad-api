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

    fun målform() =
         webClient.get()
            .uri(cf::kontaktUri)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(Kontaktinformasjon::class.java)
                .mapNotNull(Kontaktinformasjon::målform)
                .defaultIfEmpty(Målform.standard())
                .doOnError { t: Throwable -> log.warn("KRR oppslag målform feilet. Bruker default Målform", t) }
                .onErrorReturn(Målform.standard())
            .blockOptional()
            .orElse(Målform.standard())

    override fun name(): String {
        return capitalize(KRR.lowercase(Locale.getDefault()))
    }

    override fun toString(): String {
        return javaClass.simpleName + " [cfg=" + cfg + "]"
    }
}