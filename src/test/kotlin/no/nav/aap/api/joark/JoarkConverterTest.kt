package no.nav.aap.api.joark

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.aap.api.søknad.SøknadTest
import no.nav.aap.api.søknad.joark.JoarkJournalpostGenerator
import no.nav.aap.api.søknad.joark.pdf.BildeSkalerer
import no.nav.aap.api.søknad.joark.pdf.BildeTilPDFKonverterer
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.joark.Filtype.PDFA
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.util.StreamUtils.copyToByteArray
import java.io.FileOutputStream
import java.util.*

@ExtendWith(MockitoExtension::class)
@JsonTest
class JoarkConverterTest {

    @Autowired
    lateinit var mapper: ObjectMapper

    @Mock
    lateinit var lager: Dokumentlager

    @Test
    fun convert() {
        val bytes = bytesFra("pdf/test123.pdf")
        val bytes1 = bytesFra("pdf/rdd.png")
        val bytes2 = bytesFra("pdf/landscape.jpg")

        val dokinfo = DokumentInfo(bytes, APPLICATION_PDF_VALUE, "test123.pdf")
        val dokinfo1 = DokumentInfo(bytes1, IMAGE_PNG_VALUE, "rdd.png")
        val dokinfo2 = DokumentInfo(bytes2, IMAGE_JPEG_VALUE, "landscape.png")




        `when`(lager.lesDokument(anyObject()))
            .thenReturn(dokinfo)
            .thenReturn(dokinfo1)
            .thenReturn(dokinfo1)
            .thenReturn(dokinfo1)
            .thenReturn(dokinfo2)

        val søknad = SøknadTest.standardSøknad()
        val søker = SøknadTest.søker()
        val c = JoarkJournalpostGenerator(mapper, lager, BildeTilPDFKonverterer(BildeSkalerer()))
        val converted = c.journalpostFra(søknad, søker, bytes)
        converted.dokumenter.forEach { doc ->
            doc?.dokumentVarianter?.forEach {
                if (it?.filtype == PDFA.name)
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