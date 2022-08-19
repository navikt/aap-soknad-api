package no.nav.aap.api.søknad.ettersendelse

import no.nav.aap.util.LoggerUtil
import no.nav.boot.conditionals.ConditionalOnNotProd
import no.nav.security.token.support.spring.UnprotectedRestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@UnprotectedRestController(["/ettersend"])
@ConditionalOnNotProd
internal class EttersendelseController {
    private val log = LoggerUtil.getLogger(javaClass)

    @GetMapping
    fun ettersend(@RequestParam uuid: UUID) {
        log.trace("Ettersender $uuid")
    }
}