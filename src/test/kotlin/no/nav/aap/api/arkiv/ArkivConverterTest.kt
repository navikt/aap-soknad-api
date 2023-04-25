package no.nav.aap.api.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.Base64
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.util.StreamUtils.copyToByteArray
import no.nav.aap.api.OMPersoner
import no.nav.aap.api.OMSøknad
import no.nav.aap.api.oppslag.person.PDLClient
import no.nav.aap.api.søknad.arkiv.ArkivJournalpostGenerator
import no.nav.aap.api.søknad.arkiv.Journalpost.DokumentVariant.Filtype.PDFA
import no.nav.aap.api.søknad.arkiv.pdf.BildeSkalerer
import no.nav.aap.api.søknad.arkiv.pdf.PDFFraBildeFKonverterer
import no.nav.aap.api.søknad.arkiv.pdf.PDFGenerator
import no.nav.aap.api.søknad.mellomlagring.dokument.DokumentInfo
import no.nav.aap.api.søknad.mellomlagring.dokument.Dokumentlager
import no.nav.aap.api.søknad.model.Innsending
import no.nav.aap.api.søknad.model.PDFKvittering
import no.nav.security.token.support.core.context.TokenValidationContextHolder

//@ExtendWith(MockitoExtension::class)
//@JsonTest
class ArkivConverterTest {

    @Autowired
    lateinit var mapper : ObjectMapper

    @Mock
    lateinit var lager : Dokumentlager

    @Mock
    lateinit var pdl : PDLClient

    @Mock
    lateinit var ctx : TokenValidationContextHolder

    @Mock
    lateinit var pdf : PDFGenerator

    fun hentFil(fil : String) = copyToByteArray(ClassPathResource(fil).inputStream)

    //@Test
    fun convert() {

        val dokinfo = DokumentInfo(hentFil("test123.pdf"), "test123.pdf, APPLICATION_PDF_VALUE ")
        val dokinfo1 = DokumentInfo(hentFil("rdd.pdf"), "rdd.png", IMAGE_PNG_VALUE)
        val dokinfo2 = DokumentInfo(hentFil("landscape.pdf"), "landscape.png", IMAGE_JPEG_VALUE)

        `when`(lager.lesDokument(anyObject()))
            .thenReturn(dokinfo)
            .thenReturn(dokinfo1)
            .thenReturn(dokinfo1)
            .thenReturn(dokinfo1)
            .thenReturn(dokinfo2)

        val søknad = Innsending(OMSøknad.standard_soknad(), PDFKvittering(listOf(), LocalDateTime.now()))
        val søker = OMPersoner.ole_olsen()
        val c = ArkivJournalpostGenerator(pdl,mapper, lager, pdf, PDFFraBildeFKonverterer(BildeSkalerer()))
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

    private fun bytesFra(navn : String) = copyToByteArray(ClassPathResource(navn).inputStream)
}

private fun <T> anyObject() : T {
    any<T>()
    return uninitialized()
}

private fun <T> uninitialized() : T = null as T

@SpringBootApplication
internal class DummyApplication