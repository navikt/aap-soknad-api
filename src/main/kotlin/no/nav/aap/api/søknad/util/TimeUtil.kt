package no.nav.aap.api.søknad.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtil {
    fun dato(dato: String?): LocalDate? {
        return Optional.ofNullable(dato)
            .map { d: String? -> LocalDate.parse(d, DateTimeFormatter.ISO_LOCAL_DATE) }
            .orElse(null)
    }

    fun localDateTime(date: Date): LocalDateTime {
        return Instant.ofEpochMilli(date.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    fun fraDato(dato: Date): LocalDateTime {
        return Instant.ofEpochMilli(dato.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }
}