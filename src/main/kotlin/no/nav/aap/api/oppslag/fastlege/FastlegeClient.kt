package no.nav.aap.api.oppslag.fastlege

import no.nav.aap.util.LoggerUtil
import org.springframework.stereotype.Component
import java.util.*

@Component
class FastlegeClient(private val adapter: FastlegeClientAdapter) {
    private val log = LoggerUtil.getLogger(javaClass)

    fun fastlege(): Optional<Fastlege>? {
        log.info("Henter fastleger")
        var lege =  adapter.fastlege()
        log.info("Hentet fastlege $lege")
        return lege
    }
}