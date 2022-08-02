package no.nav.aap.api.søknad.fordeling

import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import java.util.*

@ConditionalOnMissingBean(SøknadFordeler::class)
class LocalSøknadFordeler : Fordeler {
    private val log = LoggerUtil.getLogger(javaClass)
    override fun fordel(søknad: UtlandSøknad) =
        Kvittering(UUID.randomUUID())
            .also {
                log.info("Dummy-ruting av utenlandssøknad til bakenforliggende systemer")
            }

    override fun fordel(søknad: StandardSøknad) =
        Kvittering(UUID.randomUUID())
            .also {
                log.info("Dummy-ruting av søknad til bakenforliggende systemer")
            }
}