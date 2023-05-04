package no.nav.aap.api.søknad.arkiv.pdf

import java.io.ByteArrayOutputStream
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle.A4
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.unit.DataSize
import org.springframework.util.unit.DataUnit.BYTES
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.størrelse

@Component
class PDFFraBildeFKonverterer(private val scaler : BildeSkalerer) {

    private val log = getLogger(javaClass)
    fun tilPdf(mediaType : MediaType, images : List<ByteArray>) = slåSammen(mediaType, *images.toTypedArray())
    fun tilPdf(mediaType : MediaType, fil : String) = tilPdf(mediaType, ClassPathResource(fil).inputStream.readBytes()) // testing only

    fun tilPdf(mediaType : MediaType, vararg bilder : ByteArray) = slåSammen(mediaType, *bilder)

    private fun slåSammen(mediaType : MediaType, vararg bilder : ByteArray) =
        runCatching {
            log.trace("Konverterer {} til PDF for {}", bilder.størrelse("bildefil"), mediaType)
            PDDocument().use { doc ->
                ByteArrayOutputStream().use { os ->
                    bilder.forEach { pdfFraBilde(doc, it, mediaType.subtype) }
                    doc.save(os)
                    os.toByteArray()
                }
            }
        }.getOrElse { e ->
            throw DokumentException("Konvertering av ${bilder.størrelse("bildefil")} av type $mediaType feilet (${
                bilder.map {
                    DataSize.of(it.size.toLong(), BYTES).toMegabytes()
                }
            })", e)
        }

    private fun pdfFraBilde(doc : PDDocument, bilde : ByteArray, fmt : String) =
        PDPage(A4).apply {
            doc.addPage(this)
            runCatching {
                PDPageContentStream(doc, this).use {
                    it.drawImage(createFromByteArray(doc, scaler.tilA4(bilde, fmt), "img"),
                        A4.lowerLeftX,
                        A4.lowerLeftY)
                }
            }.getOrElse { throw DokumentException("Konvertering av bilde med størrelse ${DataSize.of(bilde.size.toLong(), BYTES).toMegabytes()} feilet", it) }
        }
}