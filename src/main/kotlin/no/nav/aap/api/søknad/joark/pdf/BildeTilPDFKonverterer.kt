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
class BildeTilPDFKonverterer(private val scaler: BildeSkalerer) {

    private val log = getLogger(javaClass)
    fun tilPdf(bildeType: String, images: List<ByteArray>) = slåSammen(bildeType, *images.toTypedArray())
    fun tilPdf(bildeType: String, fil: String) =
        tilPdf(bildeType, ClassPathResource(fil).inputStream.readBytes()) // testing only

    fun tilPdf(bildeType: String, vararg bilder: ByteArray) = slåSammen(bildeType, *bilder)

    private fun slåSammen(bildeType: String, vararg bilder: ByteArray) =
        try {
            log.trace("Slår sammen ${bilder.size} fil(er) for $bildeType")
            PDDocument().use { doc ->
                ByteArrayOutputStream().use { os ->
                    bilder.forEach { pdfFraBilde(doc, it, parseMediaType(bildeType).subtype) }
                    doc.save(os)
                    os.toByteArray()
                }
            }
        }
        catch (e: Exception) {
            throw DokumentException(msg = "Sammenslåing/konvertering av vedlegg feilet", cause = e)
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
                throw DokumentException(msg = "Konvertering av vedlegg feilet", cause = e)
            }
        }
}