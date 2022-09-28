package no.nav.aap.api.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.api.søknad.SøknadTest
import no.nav.aap.api.søknad.arkiv.ArkivJournalpostGenerator
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.Filtype.PDFA
import no.nav.aap.api.søknad.arkiv.pdf.BildeSkalerer
import no.nav.aap.api.søknad.arkiv.pdf.PDFFraBildeFKonverterer
import no.nav.aap.api.søknad.arkiv.pdf.PDFClient
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.Innsending
import no.nav.aap.api.søknad.model.PDFKvittering
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.util.StreamUtils.copyToByteArray
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.*

//@ExtendWith(MockitoExtension::class)
//@JsonTest
class ArkivConverterTest {

    @Autowired
    lateinit var mapper: ObjectMapper

    @Mock
    lateinit var lager: Dokumentlager

    @Mock
    lateinit var ctx: TokenValidationContextHolder

    @Mock
    lateinit var pdf: PDFClient

    // @Test
    fun convert() {
        val bytes = bytesFra("pdf/test123.pdf")
        val bytes1 = bytesFra("pdf/rdd.png")
        val bytes2 = bytesFra("pdf/landscape.jpg")

        val dokinfo = DokumentInfo(bytes, "test123.pdf, APPLICATION_PDF_VALUE ")
        val dokinfo1 = DokumentInfo(bytes1, "rdd.png", IMAGE_PNG_VALUE)
        val dokinfo2 = DokumentInfo(bytes2, "landscape.png", IMAGE_JPEG_VALUE)




        `when`(lager.lesDokument(anyObject()))
            .thenReturn(dokinfo)
            .thenReturn(dokinfo1)
            .thenReturn(dokinfo1)
            .thenReturn(dokinfo1)
            .thenReturn(dokinfo2)

        val søknad = Innsending(SøknadTest.standardSøknad(), PDFKvittering(listOf(),LocalDateTime.now()))
        val søker = SøknadTest.søker()
        val c = ArkivJournalpostGenerator(mapper, lager, pdf, PDFFraBildeFKonverterer(BildeSkalerer()))
        val converted = c.journalpostFra(søknad, søker)
        converted.dokumenter.forEach { doc ->
            doc.dokumentVarianter.forEach {
                if (it.filtype == PDFA.name)
                    FileOutputStream("${it.hashCode()}.pdf").use { fos ->
                        fos.write(Base64.getDecoder().decode(it.fysiskDokument))
                    }
            }
        }
    }

    private fun bytesFra(navn: String) = copyToByteArray(ClassPathResource(navn).inputStream)

}

private fun <T> anyObject(): T {
    any<T>()
    return uninitialized()
}

private fun <T> uninitialized(): T = null as T

@SpringBootApplication
internal class DummyApplication