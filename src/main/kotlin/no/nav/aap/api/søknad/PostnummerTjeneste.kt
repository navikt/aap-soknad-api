package no.nav.aap.api.søknad

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import kotlin.text.Charsets.UTF_8

@Component
class PostnummerTjeneste(private val fil: String = "postnr.txt") {

    private val poststedFor = ClassPathResource(fil)
        .inputStream.bufferedReader(UTF_8)
        .lineSequence()
        .map { line ->
            line.split("\\s+".toRegex())
                .toTypedArray()
    }.associate { it[0] to it[1] }

    fun poststedFor(postnr: String) = poststedFor[postnr]

}