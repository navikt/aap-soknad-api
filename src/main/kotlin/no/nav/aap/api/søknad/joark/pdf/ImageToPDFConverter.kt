package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.søknad.joark.pdf.ImageScaler.downToA4
import no.nav.aap.api.søknad.mellomlagring.GCPBucketConfig.DokumentException
import no.nav.aap.util.LoggerUtil.getLogger
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle.A4
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray
import org.apache.tika.Tika
import org.slf4j.Logger
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_PDF
import org.springframework.http.MediaType.IMAGE_JPEG
import org.springframework.http.MediaType.IMAGE_PNG
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils.copyToByteArray
import java.io.ByteArrayOutputStream

@Component
class Image2PDFConverter {

    private val log: Logger = getLogger(javaClass)

    fun convert(res: String): ByteArray = convert(copyToByteArray(ClassPathResource(res).inputStream))

    fun convert(bytes: ByteArray): ByteArray =
        mediaType(bytes)?.let {
            when (it) {
                APPLICATION_PDF -> bytes
                IMAGE_JPEG, IMAGE_PNG -> embed(it.subtype, bytes)
                else -> throw DokumentException("Media type $it er ikke støttet")
            }
        } ?: throw DokumentException("Kunne ikke bestemme media type")

    private fun embed(imgType: String, vararg images: ByteArray): ByteArray =
        embed(images.toList(), imgType)

    private fun embed(images: List<ByteArray>, imgType: String) =
        try {
            PDDocument().use { doc ->
                ByteArrayOutputStream().use { os ->
                    images.forEach { addPDFPageFromImage(doc, it, imgType) }
                    doc.save(os)
                    os.toByteArray()
                }
            }
        }
        catch (e: Exception) {
            throw DokumentException("Konvertering av vedlegg feilet", e)
        }

    private fun addPDFPageFromImage(doc: PDDocument, orig: ByteArray, fmt: String) {
        val page = PDPage(A4)
        doc.addPage(page)
        try {
            PDPageContentStream(doc, page).use {
                it.drawImage(createFromByteArray(doc, downToA4(orig, fmt), "img"), A4.lowerLeftX, A4.lowerLeftY)
            }
        }
        catch (e: Exception) {
            throw DokumentException("Konvertering av vedlegg feilet", e)
        }
    }

    private fun mediaType(bytes: ByteArray?): MediaType? =
        bytes?.takeIf(ByteArray::isNotEmpty)?.let { MediaType.valueOf(Tika().detect(it)) }

}