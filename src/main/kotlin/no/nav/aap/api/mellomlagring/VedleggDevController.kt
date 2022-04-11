package no.nav.aap.api.mellomlagring

import no.nav.aap.api.søknad.AuthContextExtension.getFnr
import no.nav.aap.rest.UnprotectedRestController
import no.nav.aap.util.AuthContext
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
import java.util.*
import org.springframework.core.annotation.AliasFor
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS


@Open(value= ["/dev/vedlegg"])
class VedleggDevController(private val vedlegg: GCPVedlegg, private val ctx: AuthContext) {

    //@Unprotected
    @PostMapping(value = ["/lagre"], consumes = [MULTIPART_FORM_DATA_VALUE])
    fun lagreVedlegg(@RequestPart("vedlegg") file: MultipartFile): ResponseEntity<UUID> {
        val uuid  = vedlegg.lagre(ctx.getFnr(), file.contentType,file.bytes)
        return ResponseEntity<UUID>(uuid, CREATED)
    }
  //  @Unprotected
    @GetMapping("/les/{uuid}")
    fun lesVedlegg(@PathVariable uuid: UUID) = vedlegg.les(ctx.getFnr(), uuid)

    @DeleteMapping("/slett/{uuid}")
   // @Unprotected
    fun slettVedlegg(@PathVariable uuid: UUID): ResponseEntity<Void> {
        vedlegg.slett(ctx.getFnr(),uuid)
        return ResponseEntity<Void>(NO_CONTENT)
    }
}



@RestController
@MustBeDocumented
@Unprotected
@Target(ANNOTATION_CLASS, CLASS)
@Retention(RUNTIME)
@RequestMapping
annotation class Open(@get: AliasFor(annotation = RequestMapping::class, attribute = "value") val value:  Array<String> = ["/"],
                      @get: AliasFor(annotation = RequestMapping::class, attribute = "produces") val produces: Array<String> = [APPLICATION_JSON_VALUE])