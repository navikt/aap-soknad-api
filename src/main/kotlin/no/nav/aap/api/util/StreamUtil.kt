package no.nav.aap.api.util

import java.util.*
import java.util.stream.Stream

object StreamUtil {
    fun <T> safeStream(vararg elems: T): Stream<T> {
        return safeStream(Arrays.asList(*elems))
    }

    @JvmStatic
    fun <T> safeStream(list: List<T>): Stream<T> {
        return Optional.ofNullable(list)
            .orElseGet { java.util.List.of() }
            .stream()
    }
}