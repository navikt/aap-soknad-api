package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.søknad.joark.pdf.ImageScaler.downToA4
import no.nav.aap.api.søknad.mellomlagring.GCPBucketConfig.DokumentException
import no.nav.aap.util.LoggerUtil.getLogger
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle.A4
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray
import org.slf4j.Logger
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.parseMediaType
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils.copyToByteArray
import java.io.ByteArrayOutputStream

@Component
class Image2PDFConverter {

    private val log: Logger = getLogger(javaClass)
    fun convert(imgType: String, images: List<ByteArray>) = embed(imgType, *images.toTypedArray())
    fun convert(imgType: String, fil: String) = convert(imgType, copyToByteArray(ClassPathResource(fil).inputStream))
    fun convert(imgType: String, vararg images: ByteArray) = embed(imgType, *images)

    private fun embed(imgType: String, vararg images: ByteArray) =
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
                    it.drawImage(createFromByteArray(doc, downToA4(bilde, fmt), "img"), A4.lowerLeftX, A4.lowerLeftY)
                }
            }
            catch (e: Exception) {
                throw DokumentException("Konvertering av vedlegg feilet", e)
            }
        }
}