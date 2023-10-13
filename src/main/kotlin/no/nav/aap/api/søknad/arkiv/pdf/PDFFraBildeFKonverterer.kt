package no.nav.aap.api.søknad.arkiv.pdf

import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.util.LoggerUtil.getLogger
import no.nav.aap.util.StringExtensions.størrelse
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle.A4
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.util.Matrix
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

    fun tilPdf(bilder: List<ByteArray>): ByteArray {
        try {
            log.trace("Konverterer {} til PDF", bilder.størrelse("bildefil"))
            PDDocument(MemoryUsageSetting.setupTempFileOnly()).use { doc ->
                ByteArrayOutputStream().use { os ->
                    bilder.forEach {
                        pdfFraBilde(doc, it)
                    }
                    doc.save(os)
                    return os.toByteArray()
                }
            }
        } catch (e: Throwable) {
            val størrelser = bilder.map { DataSize.of(it.size.toLong(), BYTES).toKilobytes() }
            throw DokumentException("Konvertering av ${bilder.size} bilder feilet ($størrelser)", e)
        }
    }

    private fun pdfFraBilde(doc: PDDocument, bilde: ByteArray) {
        val pdPage = PDPage(A4)
        doc.addPage(pdPage)
        try {
            val bufferedImage = ImageIO.read(ByteArrayInputStream(bilde))

            val bildedimensjon = Bildedimensjon(bufferedImage.width, bufferedImage.height)

            val matrix = bildedimensjon.transformer(pdPage)

            val pdImg = JPEGFactory.createFromImage(doc, bufferedImage)

            PDPageContentStream(doc, pdPage).use { pdPageContentStream ->
                pdPageContentStream.drawImage(pdImg, matrix)
            }
        } catch (e: Throwable) {
            val størrelse = DataSize.of(bilde.size.toLong(), BYTES).toKilobytes()
            throw DokumentException("Konvertering av bilde med størrelse $størrelse feilet", e)
        }
    }

    private class Bildedimensjon private constructor(
        private val width: Float,
        private val height: Float,
        private val rotert: Boolean
    ) {
        constructor(width: Int, height: Int) : this(width.toFloat(), height.toFloat(), false)

        fun transformer(pdPage: PDPage): Matrix {
            val skalertBildedimensjon = roterOgSkaler(pdPage.mediaBox.width, pdPage.mediaBox.height)

            val transform = AffineTransform(
                skalertBildedimensjon.width,
                0f,
                0f,
                skalertBildedimensjon.height,
                pdPage.mediaBox.lowerLeftX,
                pdPage.mediaBox.lowerLeftY
            )
            val matrix = Matrix(transform)

            if (skalertBildedimensjon.rotert) {
                //Flytt bildet 1 gang (width) til høyre på x-aksen
                matrix.translate(1f, 0f)

                matrix.rotate(Math.toRadians(90.0))
                pdPage.rotation = 90
            }

            return matrix
        }

        private fun roteres(): Boolean {
            return width > height
        }

        private fun roterOgSkaler(pageSizeWidth: Float, pageSizeHeight: Float): Bildedimensjon {
            return if (roteres()) {
                Bildedimensjon(height, width, true).skalertDimensjon(pageSizeWidth, pageSizeHeight)
            } else {
                this.skalertDimensjon(pageSizeWidth, pageSizeHeight)
            }
        }

        private fun skalertDimensjon(pageSizeWidth: Float, pageSizeHeight: Float): Bildedimensjon {
            var width = this.width
            var height = this.height
            if (width > pageSizeWidth) {
                width = pageSizeWidth
                height = width * this.height / this.width
            }
            if (height > pageSizeHeight) {
                height = pageSizeHeight
                width = height * this.width / this.height
            }
            return Bildedimensjon(width, height, this.rotert)
        }
    }
}
