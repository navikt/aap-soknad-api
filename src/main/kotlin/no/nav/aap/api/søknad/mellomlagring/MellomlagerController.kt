package no.nav.aap.api.søknad.mellomlagring

import no.nav.aap.api.felles.SkjemaType
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.util.Constants.IDPORTEN
import no.nav.security.token.support.spring.ProtectedRestController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@ProtectedRestController(value = ["buckets"], issuer = IDPORTEN)
internal class MellomlagerController(private val mellomlager: Mellomlager, private val dokumentlager: Dokumentlager) {

    @PostMapping("/lagre/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    fun lagre(@PathVariable type: SkjemaType, @RequestBody data: String) = mellomlager.lagre(data, type)

    @GetMapping("/les/{type}")
    fun les(@PathVariable type: SkjemaType): ResponseEntity<String> {
        val les = mellomlager.les(type)
        if (les == null) return ResponseEntity<String>(HttpStatus.NO_CONTENT)
        return ResponseEntity(les, HttpStatus.OK)
    }

    @DeleteMapping("/slett/{type}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun slett(@PathVariable type: SkjemaType) = mellomlager.slett(type).also { dokumentlager.slettAlleDokumenter() }

}