package no.nav.aap.api.søknad.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

public final class TimeUtil {
    private TimeUtil() {
    }

    public static LocalDate dato(String dato) {
        return Optional.ofNullable(dato)
                .map(d -> LocalDate.parse(d, ISO_LOCAL_DATE))
                .orElse(null);
    }

    public static LocalDateTime localDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDateTime fraDato(Date dato) {
        return Instant.ofEpochMilli(dato.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
