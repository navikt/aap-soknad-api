package no.nav.aap.api.søknad.util

import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Predicate.not

object StringUtil {
    private const val DEFAULT_LENGTH = 50

    fun partialMask(value: String?): String {
        return Optional.ofNullable(value)
            .map(String::stripLeading)
            .map { v: String -> v.substring(0, v.length / 2) + "*".repeat(v.length / 2) } //TODO robustify
            .orElse("*")
    }

    @JvmOverloads
    fun limit(tekst: String?, max: Int = DEFAULT_LENGTH): String? {
        return Optional.ofNullable(tekst)
            .filter { t: String -> t.length >= max }
            .map { s: String -> s.substring(0, max - 1) + "..." }
            .orElse(tekst)
    }

    fun limit(bytes: ByteArray?, max: Int): String? {
        return limit(Arrays.toString(bytes), max)
    }

    fun mask(value: String?): String {
        return Optional.ofNullable(value)
            .map { obj: String -> obj.stripLeading() }
            .filter(not { obj: String -> obj.isBlank() })
            .map { v: String -> "*".repeat(v.length) }
            .orElse("<null>")
    }

    fun encode(string: String): String {
        return Base64.getEncoder().encodeToString(string.toByteArray(StandardCharsets.UTF_8))
    }
}