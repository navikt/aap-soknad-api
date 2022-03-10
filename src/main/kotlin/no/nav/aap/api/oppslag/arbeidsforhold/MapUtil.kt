package no.nav.aap.api.oppslag.arbeidsforhold

import java.util.*

object MapUtil {
     fun getAs(map: Map<*, *>?, key: String?): String {
        return getAs(map, key, String::class.java)
    }

     fun <T> getAs(map: Map<*, *>?, key: String?, clazz: Class<T>): T {
        return Optional.ofNullable(map)
            .map { m -> m.get(key) }
            .filter(Objects::nonNull)
            .map { obj: Any? -> clazz.cast(obj) }
            .orElse(null)
    }
}