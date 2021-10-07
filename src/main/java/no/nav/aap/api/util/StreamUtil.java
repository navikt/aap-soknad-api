package no.nav.aap.api.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class StreamUtil {
    private StreamUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> safeStream(T... elems) {
        return safeStream(Arrays.asList(elems));
    }

    public static <T> Stream<T> safeStream(List<T> list) {
        return Optional.ofNullable(list)
                .orElseGet(List::of)
                .stream();
    }
}
