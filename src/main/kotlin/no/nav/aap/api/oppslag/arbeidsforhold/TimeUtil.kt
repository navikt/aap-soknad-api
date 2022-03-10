package no.nav.aap.api.oppslag.arbeidsforhold

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.util.*

object TimeUtil {
    private val LOG: Logger = LoggerFactory.getLogger(TimeUtil::class.java)
    fun dato(dato: String?): LocalDate {
        return Optional.ofNullable(dato)
            .map { d -> LocalDate.parse(d, ISO_LOCAL_DATE) }
            .orElse(null)
    }

    fun localDateTime(date: Date): LocalDateTime {
        return Instant.ofEpochMilli(date.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    fun waitFor(delayMillis: Long) {
        try {
            LOG.trace("Venter i {}ms", delayMillis)
            Thread.sleep(delayMillis)
        } catch (e: InterruptedException) {
            throw RuntimeException("Kunne ikke vente i " + delayMillis + "ms", e)
        }
    }

    fun nowWithinPeriod(start: LocalDate?, end: LocalDate?): Boolean {
        val now =  LocalDate.now()
        return if (now.isEqual(start) || now.isEqual(end)) {
            true
        }
        else now.isAfter(start) && now.isBefore(end)
    }

    fun fraDato(dato: Date): LocalDateTime {
        return Instant.ofEpochMilli(dato.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }
}