package no.nav.aap.api.util

import java.time.Instant.ofEpochMilli
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.util.*


object TimeUtil {
    fun dato(dato: String?): LocalDate? {
        return dato?.let { d -> LocalDate.parse(d, ISO_LOCAL_DATE) }
    }
    fun fraDato(dato: Date): LocalDateTime {
        return ofEpochMilli(dato.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }
    fun localDateTime(date: Date): LocalDateTime? {
        return ofEpochMilli(date.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }
}