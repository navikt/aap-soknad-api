package no.nav.aap.api.arkiv.pdf

import no.nav.aap.api.søknad.arkiv.pdf.BildeSkalerer
import no.nav.aap.api.søknad.arkiv.pdf.PDFFraBildeFKonverterer
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import org.apache.tika.Tika
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType.APPLICATION_PDF
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_GIF
import org.springframework.http.MediaType.IMAGE_JPEG
import org.springframework.http.MediaType.IMAGE_PNG

class ImageByteArray2PDFConverterTest {

    private val converter: PDFFraBildeFKonverterer = PDFFraBildeFKonverterer(BildeSkalerer())

    private fun isPdf(bytes: ByteArray) = APPLICATION_PDF_VALUE == TIKA.detect(bytes)

    @Test
    fun jpg2Pdf() = assertTrue(isPdf(converter.tilPdf(IMAGE_JPEG, "pdf/jks.jpg")))

    @Test
    fun png2Pdf() = assertTrue(isPdf(converter.tilPdf(IMAGE_PNG, "pdf/nav-logo.png")))

    @Test
    fun gifFeil() = assertThrows<DokumentException> { converter.tilPdf(IMAGE_GIF, "pdf/loading.gif") }

    @Test
    fun junkFeil() =
        assertThrows<DokumentException> { converter.tilPdf(APPLICATION_PDF, byteArrayOf(1, 2, 3, 4)) }

    companion object {
        private val TIKA = Tika()
    }
}