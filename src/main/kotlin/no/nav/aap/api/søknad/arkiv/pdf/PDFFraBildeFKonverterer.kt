package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.størrelse
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle.A4
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.util.Matrix
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.unit.DataSize
import org.springframework.util.unit.DataUnit.BYTES
import java.awt.geom.AffineTransform
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Component
class PDFFraBildeFKonverterer {

    private val log = getLogger(javaClass)
    fun tilPdf(mediaType: MediaType, images: List<ByteArray>) = slåSammen(mediaType, *images.toTypedArray())
    fun tilPdf(mediaType: MediaType, vararg bilder: ByteArray) = slåSammen(mediaType, *bilder)

    private fun slåSammen(mediaType: MediaType, vararg bilder: ByteArray) =
        runCatching {
            log.trace("Konverterer {} til PDF for {}", bilder.størrelse("bildefil"), mediaType)
            PDDocument(MemoryUsageSetting.setupTempFileOnly()).use { doc ->
                ByteArrayOutputStream().use { os ->
                    bilder.forEach {
                        pdfFraBilde(doc, it, mediaType.subtype)
                    }
                    doc.save(os)
                    os.toByteArray()
                }
            }
        }.getOrElse { e ->
            throw DokumentException("Konvertering av ${bilder.størrelse("bildefil")} av type $mediaType feilet (${
                bilder.map {
                    DataSize.of(it.size.toLong(), BYTES).toKilobytes()
                }
            })", e)
        }

    private fun pdfFraBilde(doc: PDDocument, bilde: ByteArray, fmt: String) =
        PDPage(A4).apply {
            doc.addPage(this)
            runCatching {
                val bufferedImage = ImageIO.read(ByteArrayInputStream(bilde))
                val portrett = bufferedImage
                val img = LosslessFactory.createFromImage(doc, portrett)
                //this.rotation = 90
                PDPageContentStream(doc, this).use {
                    val roteres = portrett.height < portrett.width
                    val matrix = if (roteres) {
                        val (width, height) = skalertDimensjonPortrett(
                            Pair(portrett.width.toFloat(), portrett.height.toFloat()),
                            Pair(this.mediaBox.width, this.mediaBox.height)
                        )

                        val transform = AffineTransform(height, 0f, 0f, width, A4.lowerLeftX + height, A4.lowerLeftY)
                        val matrix = Matrix(transform)
                        matrix.rotate(Math.toRadians(90.0))
                        this.rotation = 90
                        matrix
                    } else {
                        val (width, height) = skalertDimensjon(
                            Pair(portrett.width.toFloat(), portrett.height.toFloat()),
                            Pair(this.mediaBox.width, this.mediaBox.height)
                        )

                        val transform = AffineTransform(width, 0f, 0f, height, A4.lowerLeftX, A4.lowerLeftY)
                        Matrix(transform)
                    }
                    it.drawImage(img, matrix)
                }
            }.getOrElse {
                throw DokumentException(
                    "Konvertering av bilde med størrelse ${
                        DataSize.of(
                            bilde.size.toLong(),
                            BYTES
                        ).toKilobytes()
                    } feilet", it
                )
            }
        }

    private fun skalertDimensjonPortrett(imgSize: Pair<Float, Float>, a4: Pair<Float, Float>): Pair<Float, Float> {
        var width = imgSize.first
        var height = imgSize.second
        if (imgSize.first > a4.second) {
            width = a4.second
            height = width * imgSize.second / imgSize.first
        }
        if (height > a4.first) {
            height = a4.first
            width = height * imgSize.first / imgSize.second
        }
        return Pair(width, height)
    }

    private fun skalertDimensjon(imgSize: Pair<Float, Float>, a4: Pair<Float, Float>): Pair<Float, Float> {
        var width = imgSize.first
        var height = imgSize.second
        if (imgSize.first > a4.first) {
            width = a4.first
            height = width * imgSize.second / imgSize.first
        }
        if (height > a4.second) {
            height = a4.second
            width = height * imgSize.first / imgSize.second
        }
        return Pair(width, height)
    }
}