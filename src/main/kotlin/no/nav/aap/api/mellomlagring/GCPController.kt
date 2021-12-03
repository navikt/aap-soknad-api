package no.nav.aap.api.mellomlagring

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.søknad.SkjemaType
import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
@RequestMapping(value = ["buckets"], produces = [APPLICATION_JSON_VALUE])
@ConditionalOnDevOrLocal
class GCPController(private val gcp: GCPMellomlagring) {

    @PostMapping("/lagre/{fnr}")
    fun lagre(@PathVariable fnr: Fødselsnummer, @PathVariable type: SkjemaType, @RequestBody data: String) =
        gcp.lagre(fnr, type, data)

    @GetMapping("/les/{fnr}")
    fun les(@PathVariable fnr: Fødselsnummer, @PathVariable type: SkjemaType, @RequestParam data: String) =
        gcp.les(fnr, type)

    @PostMapping("/slett/{fnr}")
    fun slett(@PathVariable fnr: Fødselsnummer, @PathVariable type: SkjemaType) = gcp.slett(fnr, type)
}