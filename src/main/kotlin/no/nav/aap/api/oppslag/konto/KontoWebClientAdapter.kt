package no.nav.aap.api.oppslag.konto

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.felles.Kontonummer
import no.nav.aap.api.oppslag.konto.KontoConfig.Companion.KONTO
import no.nav.aap.rest.AbstractWebClientAdapter
import no.nav.aap.util.AuthContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDateTime

@Component
class KontoWebClientAdapter(@Qualifier(KONTO) client: WebClient,
                            private val ctx: AuthContext,
                            private val cf: KontoConfig) :
    AbstractWebClientAdapter(client, cf) {

    fun kontoInformasjon(historikk: Boolean = false) =
        if (cf.isEnabled) {
            webClient.post()
                .uri(cf::kontoUri)
                .bodyValue(KontoQuery(ctx.getFnr(), historikk))
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono<AktivKonto>()
                .doOnSuccess {
                    log.trace("Kontoinformasjon er $it")
                }
                .doOnError { t: Throwable ->
                    log.warn("Kontoinformasjon oppslag feilet", t)
                }
                .onErrorReturn(AktivKonto.NONE)
                .block()?.let { it.aktivKonto?.kontonummer }
        }
        else null

    internal data class AktivKonto(val aktivKonto: Kontoinformasjon?) {
        internal class Kontoinformasjon(val kontohaver: Fødselsnummer,
                                        val kontonummer: Kontonummer,
                                        val gyldigFom: LocalDateTime,
                                        val opprettetAv: String)

        companion object {
            val NONE = AktivKonto(null)
        }
    }

    internal data class KontoQuery(val kontohaver: Fødselsnummer, val historikk: Boolean)
}