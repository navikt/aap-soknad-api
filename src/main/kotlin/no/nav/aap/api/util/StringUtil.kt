package no.nav.aap.api.util

import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

object StringUtil {
    private const val DEFAULT_LENGTH = 50

    fun partialMask(value: String?): String {
        val start = value?.length?.div(2) ?: 0
        val end = value?.length ?: 0
        return value?.takeIf { it.isNotEmpty() }?.replaceRange(start + 1,end ,"*".repeat(end -(start + 1))) ?: "<null>"
    }

    fun limit(value: String?, max: Int = DEFAULT_LENGTH) = value?.takeIf { it.length >= max }?.take(max)?.padEnd(3, '.') ?: value
    fun limit(bytes: ByteArray?, max: Int = DEFAULT_LENGTH) = limit(Arrays.toString(bytes), max)
    fun mask(value: String?,  mask: String = "*") =  value?.replace(("[^\\.]").toRegex(), mask) ?: "<null>"
    fun encode(string: String) = Base64.getEncoder().encodeToString(string.toByteArray(UTF_8))
}