package no.nav.aap.api.arkiv.pdf

import no.nav.aap.api.søknad.arkiv.pdf.BildeSkalerer
import org.apache.tika.Tika
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.util.StreamUtils.copyToByteArray
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

internal class ImageScalerTest {

    @Test
    @Throws(Exception::class)
    fun imgSmallerThanA4RemainsUnchanged() =
        with(bytesFra("/pdf/jks.jpg")) {
            assertThat(BildeSkalerer().tilA4(this, "jpg")).hasSameSizeAs(this)
        }

    @Test
    @Throws(Exception::class)
    fun imgBiggerThanA4IsScaledDown() {
        val orig = bytesFra("/pdf/rdd.png")
        val origImg = fromBytes(orig)
        val scaledImg = fromBytes(BildeSkalerer().tilA4(orig, "jpg"))
        assertThat(scaledImg.width).isLessThan(origImg.width)
        assertThat(scaledImg.height).isLessThan(origImg.height)
    }

    @Test
    @Throws(Exception::class)
    fun scaledImgHasRetainedFormat() = assertTrue(isJpg(BildeSkalerer().tilA4(bytesFra("/pdf/rdd.png"), "jpg")))

    @Test
    @Throws(Exception::class)
    fun rotateLandscapeToPortrait() {
        val orig = bytesFra("/pdf/landscape.jpg")
        val origImg = fromBytes(orig)
        val scaledImg = fromBytes(BildeSkalerer().tilA4(orig, "jpg"))
        assertThat(origImg.width).isGreaterThan(origImg.height)
        assertThat(scaledImg.height).isGreaterThan(scaledImg.width)
    }

    private fun isJpg(bytes: ByteArray) = IMAGE_JPEG_VALUE == TIKA.detect(bytes)

    private fun fromBytes(bytes: ByteArray) = ImageIO.read(ByteArrayInputStream(bytes))

    companion object {
        private val TIKA = Tika()
        private fun bytesFra(navn: String) = copyToByteArray(ClassPathResource(navn).inputStream)
    }
}