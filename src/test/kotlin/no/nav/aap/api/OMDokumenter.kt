package no.nav.aap.api

import org.springframework.core.io.ClassPathResource
import org.springframework.util.StreamUtils.copyToByteArray

class OMDokumenter {

    companion object {
        fun lag_stor_fil():ByteArray{
            return copyToByteArray(ClassPathResource("pdf/stor.pdf").inputStream)
        }

        fun med_passord():ByteArray{
            return copyToByteArray(ClassPathResource("pdf/medpassord.pdf").inputStream)
        }

        fun test123():ByteArray{
            return copyToByteArray(ClassPathResource("pdf/test123.pdf").inputStream)
        }

        fun rdd():ByteArray{
            return copyToByteArray(ClassPathResource("pdf/rdd.png").inputStream)
        }

        fun landscape():ByteArray{
            return copyToByteArray(ClassPathResource("pdf/landscape.jpg").inputStream)
        }
    }

}