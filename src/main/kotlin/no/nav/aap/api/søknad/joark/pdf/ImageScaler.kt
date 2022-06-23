package no.nav.aap.api.søknad.joark.pdf

import no.nav.aap.api.søknad.mellomlagring.GCPBucketConfig.DokumentException
import org.apache.pdfbox.pdmodel.common.PDRectangle.A4
import org.slf4j.LoggerFactory
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
import java.io.IOException
import javax.imageio.ImageIO
import javax.imageio.ImageIO.read

internal object ImageScaler {
    private val LOG = LoggerFactory.getLogger(ImageScaler::class.java)
    fun downToA4(origImage: ByteArray, format: String): ByteArray {
        return try {
            var image = read(ByteArrayInputStream(origImage))
            image = rotatePortrait(image)
            val pdfPageDim = Dimension(A4.width.toInt(), A4.height.toInt())
            val origDim = Dimension(image.width, image.height)
            val newDim = getScaledDimension(origDim, pdfPageDim)
            if (newDim == origDim) {
                origImage
            }
            else {
                toBytes(scaleDown(image, newDim), format)
            }
        }
        catch (ex: IOException) {
            throw DokumentException("Konvertering av vedlegg feilet", ex)
        }
    }

    private fun rotatePortrait(image: BufferedImage): BufferedImage {
        if (image.height >= image.width) {
            return image
        }
        if (image.type == TYPE_CUSTOM) {
            LOG.info("Kan ikke rotere bilde med ukjent type")
            return image
        }
        var rotatedImage = BufferedImage(image.height, image.width, image.type)
        return with(AffineTransform()) {
            this.rotate(Math.toRadians(90.0), (image.height / 2f).toDouble(), (image.height / 2f).toDouble())
            AffineTransformOp(this, TYPE_BILINEAR).filter(image, rotatedImage)
        }
    }

    private fun getScaledDimension(imgSize: Dimension, a4: Dimension): Dimension {
        val originalWidth = imgSize.width
        val originalHeight = imgSize.height
        var newWidth = originalWidth
        var newHeight = originalHeight
        if (originalWidth > a4.width) {
            newWidth = a4.width
            newHeight = newWidth * originalHeight / originalWidth
        }
        if (newHeight > a4.height) {
            newHeight = a4.height
            newWidth = newHeight * originalWidth / originalHeight
        }
        return Dimension(newWidth, newHeight)
    }

    private fun scaleDown(origImage: BufferedImage, newDim: Dimension): BufferedImage {
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

    @Throws(IOException::class)
    private fun toBytes(img: BufferedImage, format: String) =
        with(ByteArrayOutputStream()) {
            ImageIO.write(img, format, this)
            this.toByteArray()
        }
}