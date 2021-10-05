package no.nav.aap.api.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static java.util.function.Predicate.not;

public final class StringUtil {
    private static final int DEFAULT_LENGTH = 50;

    private StringUtil() {
    }

    public static String taint(String value) {
        if (!value.matches("[a-zA-Z0-9]++"))
            throw new IllegalArgumentException(value);
        return value;
    }



    public static String limit(String tekst) {
        return limit(tekst, DEFAULT_LENGTH);
    }

    public static String limit(String tekst, int max) {
        return Optional.ofNullable(tekst)
                .filter(t -> t.length() >= max)
                .map(s -> s.substring(0, max - 1) + "...")
                .orElse(tekst);
    }

    public static String limit(byte[] bytes, int max) {
        return limit(Arrays.toString(bytes), max);
    }


    public static String mask(String value) {
        return Optional.ofNullable(value)
                .map(String::stripLeading)
                .filter(not(String::isBlank))
                .map(v -> "*".repeat(v.length()))
                .orElse("<null>");
    }

    public static String encode(String string) {
        try {
            return Base64.getEncoder().encodeToString(string.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
