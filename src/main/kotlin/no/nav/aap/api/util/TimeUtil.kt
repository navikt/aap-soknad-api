package no.nav.aap.api.util

import java.time.Instant.ofEpochMilli
import java.time.LocalDateTime.ofInstant
import java.time.ZoneId.systemDefault
import java.time.format.DateTimeFormatter.ofPattern

object TimeUtil {
    fun format(time: Long, fmt: String = "yyyy-MM-dd HH:mm:ss") = ofInstant(ofEpochMilli(time), systemDefault()).format(ofPattern(fmt))
}