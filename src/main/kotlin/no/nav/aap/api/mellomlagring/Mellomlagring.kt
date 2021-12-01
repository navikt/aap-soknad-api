package no.nav.aap.api.mellomlagring

interface Mellomlagring {
    fun lagre(katalog: String, key: String, value: String)
    fun les(katalog: String, key: String): String?
    fun slett(katalog: String, key: String): Boolean
}