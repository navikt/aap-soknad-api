package no.nav.aap.api.joark.pdf

import junit.framework.TestCase.assertEquals
import no.nav.aap.api.søknad.joark.pdf.Image2PDFConverter
import no.nav.aap.api.søknad.mellomlagring.GCPBucketConfig.DokumentException
import org.apache.tika.Tika
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_GIF_VALUE
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE

class ImageByteArray2PDFConverterTest {

    private val converter: Image2PDFConverter = Image2PDFConverter()

    private fun isPdf(bytes: ByteArray) = APPLICATION_PDF_VALUE == TIKA.detect(bytes)

    @Test
    fun jpg2Pdf() = assertTrue(isPdf(converter.convert(IMAGE_JPEG_VALUE, "pdf/jks.jpg")))

    @Test
    fun png2Pdf() = assertTrue(isPdf(converter.convert(IMAGE_PNG_VALUE, "pdf/nav-logo.png")))

    @Test
    fun gifFeil() = assertThrows<DokumentException> { converter.convert(IMAGE_GIF_VALUE, "pdf/loading.gif") }

    @Test
    fun pdf2Pdf() =
        assertEquals(APPLICATION_PDF_VALUE, TIKA.detect(converter.convert(APPLICATION_PDF_VALUE, "pdf/test123.pdf")))

    @Test
    fun junkFeil() =
        assertThrows<DokumentException> { converter.convert(APPLICATION_PDF_VALUE, byteArrayOf(1, 2, 3, 4)) }

    companion object {
        private val TIKA = Tika()
    }
}