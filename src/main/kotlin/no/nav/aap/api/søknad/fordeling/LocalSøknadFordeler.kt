package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.søknad.fordeling.SøknadFordeler.Kvittering
import no.nav.aap.api.søknad.model.Innsending
import no.nav.aap.api.søknad.model.StandardEttersending
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil.getLogger
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean

@ConditionalOnMissingBean(SøknadFordeler::class)
class LocalSøknadFordeler : Fordeler {
    private val log = getLogger(javaClass)
    override fun fordel(søknad: UtlandSøknad) =
        Kvittering("42")
            .also {
                log.info("Dummy-ruting av utenlandssøknad til bakenforliggende systemer")
            }

    override fun fordel(innsending: Innsending) =
        Kvittering("42").also {
            log.info("Dummy-ruting av søknad til bakenforliggende systemer")
        }

    override fun fordel(e: StandardEttersending) =
        Kvittering("42")
}