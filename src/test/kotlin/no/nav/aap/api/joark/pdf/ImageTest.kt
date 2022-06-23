package no.nav.aap.api.joark.pdf

import junit.framework.TestCase.assertEquals
import no.nav.aap.api.søknad.joark.pdf.Image2PDFConverter
import no.nav.aap.api.søknad.virus.AttachmentException
import org.apache.tika.Tika
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE

class ImageByteArray2PDFConverterTest {

    private val converter: Image2PDFConverter = Image2PDFConverter()

    private fun isPdf(bytes: ByteArray) = APPLICATION_PDF_VALUE == TIKA.detect(bytes)

    @Test
    fun jpg2Pdf() = assertTrue(isPdf(converter.convert("pdf/jks.jpg")))

    @Test
    fun png2Pdf() = assertTrue(isPdf(converter.convert("pdf/nav-logo.png")))

    @Test
    fun gifFeil() = assertThrows<AttachmentException> { converter.convert("pdf/loading.gif") }

    @Test
    fun pdf2Pdf() = assertEquals(APPLICATION_PDF_VALUE, TIKA.detect(converter.convert("pdf/test123.pdf")))

    @Test
    fun junkFeil() = assertThrows<AttachmentException> { converter.convert(byteArrayOf(1, 2, 3, 4)) }

    companion object {
        private val TIKA = Tika()
    }
}