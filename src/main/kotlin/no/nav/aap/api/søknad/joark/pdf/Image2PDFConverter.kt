package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.util.LoggerUtil.getLogger
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle.A4
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.parseMediaType
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream

@Component
class Image2PDFConverter(private val scaler: ImageScaler) {

    private val log = getLogger(javaClass)
    fun tilPdf(imgType: String, images: List<ByteArray>) = slåSammen(imgType, *images.toTypedArray())
    fun tilPdf(imgType: String, fil: String) =
        tilPdf(imgType, ClassPathResource(fil).inputStream.readBytes()) // testing only

    fun tilPdf(imgType: String, vararg images: ByteArray) = slåSammen(imgType, *images)

    private fun slåSammen(imgType: String, vararg images: ByteArray) =
        try {
            log.trace("Slår sammen ${images.size} fil(er) for $imgType")
            PDDocument().use { doc ->
                ByteArrayOutputStream().use { os ->
                    images.forEach { pdfFraBilde(doc, it, parseMediaType(imgType).subtype) }
                    doc.save(os)
                    os.toByteArray()
                }
            }
        }
        catch (e: Exception) {
            throw DokumentException("Sammenslåing/konvertering av vedlegg feilet", e)
        }

    private fun pdfFraBilde(doc: PDDocument, bilde: ByteArray, fmt: String) =
        PDPage(A4).apply {
            doc.addPage(this)
            try {
                PDPageContentStream(doc, this).use {
                    it.drawImage(createFromByteArray(doc, scaler.tilA4(bilde, fmt), "img"),
                            A4.lowerLeftX,
                            A4.lowerLeftY)
                }
            }
            catch (e: Exception) {
                throw DokumentException("Konvertering av vedlegg feilet", e)
            }
        }
}