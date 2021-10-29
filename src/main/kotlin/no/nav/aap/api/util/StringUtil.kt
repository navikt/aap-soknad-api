package no.nav.aap.api.util

import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import java.util.function.Predicate.not

object StringUtil {
    private const val DEFAULT_LENGTH = 50

    fun partialMask(value: String?): String {
        return Optional.ofNullable(value)
            .map(String::stripLeading)
            .map { v: String -> v.substring(0, v.length / 2) + "*".repeat(v.length / 2) } // robustify
            .orElse("*")
    }

    fun limit(tekst: String?, max: Int = DEFAULT_LENGTH) =
        tekst?.takeIf { it.length >= max }?.take(max)?.padEnd(3, '.') ?: tekst

    fun limit(bytes: ByteArray?, max: Int= DEFAULT_LENGTH): String? {
        return limit(Arrays.toString(bytes), max)
    }

    fun mask(value: String?): String {
        value?.trim().takeIf { it!!.isNotBlank() }
        return Optional.ofNullable(value)
            .map { obj: String -> obj.stripLeading() }
            .filter(not { obj: String -> obj.isBlank() })
            .map { v: String -> "*".repeat(v.length) }
            .orElse("<null>")
    }

    fun encode(string: String): String {
        return Base64.getEncoder().encodeToString(string.toByteArray(UTF_8))
    }
}