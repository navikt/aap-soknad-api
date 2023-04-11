package no.nav.aap.api.søknad.arkiv.pdf

import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.AffineTransformOp.TYPE_BILINEAR
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.SCALE_SMOOTH
import java.awt.image.BufferedImage.TYPE_3BYTE_BGR
import java.awt.image.BufferedImage.TYPE_CUSTOM
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.Math.toRadians
import javax.imageio.ImageIO.read
import javax.imageio.ImageIO.write
import org.apache.pdfbox.pdmodel.common.PDRectangle.A4
import org.springframework.stereotype.Component
import no.nav.aap.api.søknad.mellomlagring.DokumentException
import no.nav.aap.util.LoggerUtil.getLogger

@Component
class BildeSkalerer {
    private val log = getLogger(javaClass)
    fun tilA4(origImage: ByteArray, format: String) =
        runCatching {
            with(tilPortrett(read(ByteArrayInputStream(origImage)))) {
                val origDim = Dimension(width, height)
                val newDim = skalertDimensjon(origDim, Dimension(A4.width.toInt(), A4.height.toInt()))
                if (newDim == origDim) {
                    origImage
                }
                else {
                    bytes(skalerNed(this, newDim), format)
                }
            }
        }.getOrElse { throw DokumentException("Konvertering av vedlegg feilet", it) }

    private fun tilPortrett(image: BufferedImage): BufferedImage {
        if (image.height >= image.width) {
            return image
        }
        if (TYPE_CUSTOM == image.type) {
            log.warn("Kan ikke rotere bilde med ukjent type")
            return image
        }
        return with(AffineTransform()) {
            rotate(toRadians(90.0), (image.height / 2f).toDouble(), (image.height / 2f).toDouble())
            AffineTransformOp(this, TYPE_BILINEAR).filter(image, BufferedImage(image.height, image.width, image.type))
        }
    }

    private fun skalertDimensjon(imgSize: Dimension, a4: Dimension): Dimension {
        var width = imgSize.width
        var height = imgSize.height
        if (imgSize.width > a4.width) {
            width = a4.width
            height = width * imgSize.height / imgSize.width
        }
        if (height > a4.height) {
            height = a4.height
            width = height * imgSize.width / imgSize.height
        }
        return Dimension(width, height)
    }

    private fun skalerNed(origImage: BufferedImage, newDim: Dimension): BufferedImage {
        val newWidth = newDim.getWidth().toInt()
        val newHeight = newDim.getHeight().toInt()
        val tempImg = origImage.getScaledInstance(newWidth, newHeight, SCALE_SMOOTH)
        val scaledImg = BufferedImage(newWidth, newHeight, TYPE_3BYTE_BGR)
        (scaledImg.graphics as Graphics2D).apply {
            drawImage(tempImg, 0, 0, null)
            dispose()
        }
        return scaledImg
    }

    private fun bytes(img: BufferedImage, format: String) =
        with(ByteArrayOutputStream()) {
            write(img, format, this)
            toByteArray()
        }
}