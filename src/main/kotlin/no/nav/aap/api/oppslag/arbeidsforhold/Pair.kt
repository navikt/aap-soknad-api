package no.nav.aap.api.oppslag.arbeidsforhold

import java.util.*

class Pair<T1, T2> private constructor(val first: T1, val second: T2) {

    override fun hashCode(): Int {
        return Objects.hash(first, second)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as Pair<*, *>
        return Objects.equals(first, other.first) && Objects.equals(second, other.second)
    }

    override fun toString(): String {
        return javaClass.simpleName + " [first=" + first + ", second=" + second + "]"
    }

    companion object {
        fun <T1, T2> of(first: T1, second: T2): Pair<T1, T2> {
            return Pair(first, second)
        }
    }
}