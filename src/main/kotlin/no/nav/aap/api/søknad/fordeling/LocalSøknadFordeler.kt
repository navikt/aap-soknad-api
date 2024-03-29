package no.nav.aap.api.søknad.fordeling

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import no.nav.aap.api.søknad.fordeling.SøknadFordeler.Kvittering
import no.nav.aap.util.LoggerUtil.getLogger

@ConditionalOnMissingBean(SøknadFordeler::class)
class LocalSøknadFordeler : Fordeler {

    private val log = getLogger(javaClass)

    override fun fordel(innsending : Innsending) =
        Kvittering("42").also {
            log.info("Dummy-ruting av søknad til bakenforliggende systemer")
        }

    override fun fordel(e : Ettersending) =
        Kvittering("42")
}