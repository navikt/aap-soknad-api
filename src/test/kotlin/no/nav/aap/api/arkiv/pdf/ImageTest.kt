package no.nav.aap.api.arkiv.pdf

import no.nav.aap.api.søknad.arkiv.pdf.BildeSkalerer
import no.nav.aap.api.søknad.arkiv.pdf.BildeTilPDFKonverterer
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import org.apache.tika.Tika
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_GIF_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE

class ImageByteArray2PDFConverterTest {

    private val converter: BildeTilPDFKonverterer = BildeTilPDFKonverterer(BildeSkalerer())

    private fun isPdf(bytes: ByteArray) = APPLICATION_PDF_VALUE == TIKA.detect(bytes)

    @Test
    fun jpg2Pdf() = assertTrue(isPdf(converter.tilPdf(IMAGE_JPEG_VALUE, "pdf/jks.jpg")))

    @Test
    fun png2Pdf() = assertTrue(isPdf(converter.tilPdf(IMAGE_PNG_VALUE, "pdf/nav-logo.png")))

    @Test
    fun gifFeil() = assertThrows<DokumentException> { converter.tilPdf(IMAGE_GIF_VALUE, "pdf/loading.gif") }

    @Test
    fun junkFeil() =
        assertThrows<DokumentException> { converter.tilPdf(APPLICATION_PDF_VALUE, byteArrayOf(1, 2, 3, 4)) }

    companion object {
        private val TIKA = Tika()
    }
}