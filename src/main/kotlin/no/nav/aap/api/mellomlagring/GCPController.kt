package no.nav.aap.api.mellomlagring

import no.nav.boot.conditionals.ConditionalOnDevOrLocal
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
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

    @PostMapping("/lagre")
    fun lagre(@RequestBody data: String) = gcp.lagre("katalog", "key", data)

    @GetMapping("/les")
    fun les(@RequestParam data: String) = gcp.les("katalog", "key")

    @PostMapping("/slett")
    fun slett() = gcp.slett("katalog", "key")
}