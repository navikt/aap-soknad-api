package no.nav.aap.api.mellomlagring.dev

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.mellomlagring.GCPVedlegg
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@Unprotected
@RestController
@RequestMapping(value= ["/dev/jalla/"])
class VedleggDevController(private val vedlegg: GCPVedlegg) {

    @Unprotected
    @PostMapping(value = ["lagre/{fnr}"], consumes = [MULTIPART_FORM_DATA_VALUE])
    fun lagreVedlegg(@PathVariable fnr: Fødselsnummer, @RequestPart("vedlegg") file: MultipartFile): ResponseEntity<UUID> {
        val uuid  = vedlegg.lagre(fnr, file.contentType,file.bytes)
        return ResponseEntity<UUID>(uuid, CREATED)
    }
    @Unprotected
    @GetMapping("les/{fnr}/{uuid}")
    fun lesVedlegg(@PathVariable fnr: Fødselsnummer,@PathVariable uuid: UUID) = vedlegg.les(fnr, uuid)

    @Unprotected
    @DeleteMapping("slett/{fnr}/{uuid}")
    fun slettVedlegg(@PathVariable fnr: Fødselsnummer,@PathVariable uuid: UUID): ResponseEntity<Void> {
        vedlegg.slett(fnr,uuid)
        return ResponseEntity<Void>(NO_CONTENT)
    }
}