package no.nav.aap.api.arkiv.pdf

import no.nav.aap.api.søknad.arkiv.pdf.PDFFraBildeFKonverterer
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import org.apache.tika.Tika
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_PDF
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.MediaType.IMAGE_GIF
import org.springframework.http.MediaType.IMAGE_JPEG
import org.springframework.http.MediaType.IMAGE_PNG
import java.io.File

class ImageByteArray2PDFConverterTest {

    private val converter: PDFFraBildeFKonverterer = PDFFraBildeFKonverterer()

    private fun isPdf(bytes: ByteArray) = APPLICATION_PDF_VALUE == TIKA.detect(bytes)

    private fun PDFFraBildeFKonverterer.tilPdf(vararg fil: String) =
        tilPdf(fil.map { ClassPathResource(it).inputStream.readBytes() })

    @Test
    fun jpg2Pdf() = assertTrue(isPdf(converter.tilPdf("pdf/jks.jpg")))

    @Test
    fun png2Pdf() {
        val pdf = converter.tilPdf("pdf/nav-logo.png", "pdf/rdd.png")
        assertTrue(isPdf(pdf))
    }

    @Test
    fun gifFeil() = assertThrows<DokumentException> { converter.tilPdf("pdf/loading.gif") }

    @Test
    fun junkFeil() =
        assertThrows<DokumentException> { converter.tilPdf(listOf(byteArrayOf(1, 2, 3, 4))) }

    companion object {
        private val TIKA = Tika()
    }
}