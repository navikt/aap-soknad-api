package no.nav.aap.api.mellomlagring.dev

import no.nav.aap.api.felles.Fødselsnummer
import no.nav.aap.api.mellomlagring.GCPVedlegg
import no.nav.aap.api.mellomlagring.GCPVedlegg.Companion.FILNAVN
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CACHE_CONTROL
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.HttpHeaders.EXPIRES
import org.springframework.http.HttpHeaders.PRAGMA
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.MediaType.parseMediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID



@Unprotected
@RestController
@RequestMapping(value= ["/dev/vedlegg/"])
class VedleggDevController(private val vedlegg: GCPVedlegg) {


    @PostMapping(value = ["lagre/{fnr}"], consumes = [MULTIPART_FORM_DATA_VALUE])
    fun lagreVedlegg(@PathVariable fnr: Fødselsnummer, @RequestPart("vedlegg") file: MultipartFile): ResponseEntity<UUID> {
        val uuid = UUID.randomUUID()
        return ResponseEntity.created(vedlegg.lagreVedlegg(fnr, uuid, file)).body(uuid)
    }

    @GetMapping(path= ["les/{fnr}/{uuid}"])
    fun lesVedlegg(@PathVariable fnr: Fødselsnummer,@PathVariable uuid: UUID) =
         vedlegg.lesVedlegg(fnr, uuid)?.let {
            ResponseEntity<ByteArray>(
                    it.getContent(),
                    HttpHeaders().apply {
                        add(EXPIRES, "0")
                        add(PRAGMA, "no-cache")
                        add(CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                        add(CONTENT_DISPOSITION, "attachment; filename=${it.metadata[FILNAVN]}")
                        contentType = parseMediaType(it.contentType)
                    },
                    OK)
        } ?: ResponseEntity<ByteArray>(NOT_FOUND)

    @DeleteMapping("slett/{fnr}/{uuid}")
    fun slettVedlegg(@PathVariable fnr: Fødselsnummer,@PathVariable uuid: UUID): ResponseEntity<Void> {
        vedlegg.slettVedlegg(fnr,uuid)
        return ResponseEntity<Void>(NO_CONTENT)
    }
}