package no.nav.aap.api.oppslag.krr

import no.nav.aap.api.oppslag.krr.KRRConfig.Companion.KRR
import no.nav.aap.api.oppslag.krr.Målform.Companion
import no.nav.aap.api.oppslag.krr.Målform.EN
import no.nav.aap.api.oppslag.krr.Målform.NB
import no.nav.aap.rest.AbstractWebClientAdapter
import org.apache.commons.lang3.StringUtils.capitalize
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.*


@Component
class KRRWebClientAdapter(@Qualifier(KRR) client: WebClient, val cf: KRRConfig) :
    AbstractWebClientAdapter(client, cf) {


    fun målform(): Målform {
        LOG.info("Henter målform fra KRR")
        return webClient.get()
            .uri(cf::kontaktUri)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(Kontaktinformasjon::class.java)
                .mapNotNull(Kontaktinformasjon::målform)
                .defaultIfEmpty(Målform.standard())
                .doOnError { t: Throwable -> LOG.warn("KRR oppslag målform feilet. Bruker default Målform", t) }
                .onErrorReturn(Målform.standard())
            .blockOptional()
            .orElse(Målform.standard())
    }

    override fun name(): String {
        return capitalize(KRR.lowercase(Locale.getDefault()))
    }

    override fun toString(): String {
        return javaClass.simpleName + " [cfg=" + cfg + "]"
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(KRRWebClientAdapter::class.java)
    }
}