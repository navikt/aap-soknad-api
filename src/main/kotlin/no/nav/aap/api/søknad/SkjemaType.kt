package no.nav.aap.api.søknad

enum class SkjemaType(val kode: String, val tittel: String) {
    UTLAND("NAV 11-03.07", "Søknad om å beholde AAP ved opphold i utlandet"),
    HOVED("NAV 11-13.05", "Søknad om AAP")
}