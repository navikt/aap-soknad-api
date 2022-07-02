package no.nav.aap.api.søknad.routing

import no.nav.aap.api.søknad.model.Kvittering
import no.nav.aap.api.søknad.model.StandardSøknad
import no.nav.aap.api.søknad.model.UtlandSøknad
import no.nav.aap.util.LoggerUtil
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import java.util.*

@ConditionalOnMissingBean(SøknadRouter::class)
class LocalSøknadRouter : Router {
    private val log = LoggerUtil.getLogger(javaClass)
    override fun route(søknad: UtlandSøknad) =
        Kvittering(UUID.randomUUID())
            .also {
                log.info("Dummy-ruting av utenlandssøknad til bakenforliggende systemer")
            }

    override fun route(søknad: StandardSøknad) =
        Kvittering(UUID.randomUUID())
            .also {
                log.info("Dummy-ruting av søknad til bakenforliggende systemer")
            }
}