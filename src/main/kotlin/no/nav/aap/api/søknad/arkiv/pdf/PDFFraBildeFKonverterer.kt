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
                        pdfFraBilde(doc, it)
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

    private fun pdfFraBilde(doc: PDDocument, bilde: ByteArray) {
        val pdPage = PDPage(A4)
        doc.addPage(pdPage)
        try {
            val bufferedImage = ImageIO.read(ByteArrayInputStream(bilde))

            val roteres = bufferedImage.height < bufferedImage.width

            val (width, height) = skalertDimensjon(
                MyPair(bufferedImage.width.toFloat(), bufferedImage.height.toFloat()).roterHvis(roteres),
                MyPair(pdPage.mediaBox.width, pdPage.mediaBox.height),
            )

            val transform = AffineTransform(width, 0f, 0f, height, A4.lowerLeftX, A4.lowerLeftY)
            val matrix = Matrix(transform)

            if (roteres) {
                //Flytt bildet 1 gang (width) til høyre på x-aksen
                matrix.translate(1f, 0f)

                matrix.rotate(Math.toRadians(90.0))
                pdPage.rotation = 90
            }

            val pdImg = LosslessFactory.createFromImage(doc, bufferedImage)

            PDPageContentStream(doc, pdPage).use { pdPageContentStream ->
                pdPageContentStream.drawImage(pdImg, matrix)
            }
        } catch (e: Throwable) {
            val størrelse = DataSize.of(bilde.size.toLong(), BYTES).toKilobytes()
            throw DokumentException("Konvertering av bilde med størrelse $størrelse feilet", e)
        }
    }

    private data class MyPair(val width: Float, val height: Float) {
        fun roterHvis(roteres: Boolean): MyPair {
            return if (roteres) {
                MyPair(height, width)
            } else {
                this
            }
        }
    }

    private fun skalertDimensjon(imgSize: MyPair, a4: MyPair): MyPair {
        var (width, height) = imgSize
        if (width > a4.width) {
            width = a4.width
            height = width * imgSize.height / imgSize.width
        }
        if (height > a4.height) {
            height = a4.height
            width = height * imgSize.width / imgSize.height
        }
        return MyPair(width, height)
    }
}
