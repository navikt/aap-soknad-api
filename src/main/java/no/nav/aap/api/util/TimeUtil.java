package no.nav.aap.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

public final class TimeUtil {
    private static final Logger LOG = LoggerFactory.getLogger(TimeUtil.class);

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
